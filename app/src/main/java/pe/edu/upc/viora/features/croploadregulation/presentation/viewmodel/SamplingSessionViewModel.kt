package pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.fold
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SubmitSamplingBatchUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.croploadregulation.presentation.state.SamplingSessionUiState

@HiltViewModel
class SamplingSessionViewModel @Inject constructor(
    private val submitSamplingBatch: SubmitSamplingBatchUseCase,
    private val thinningRepository: ThinningRepository,
    private val draftDao: DraftTreeSampleDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SamplingSessionUiState())
    val uiState: StateFlow<SamplingSessionUiState> = _uiState.asStateFlow()

    private fun isDeviceOffline(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return true
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return true
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    fun initSession(plotId: String, plotName: String, campaignYear: Int = java.time.Year.now().value) {
        val currentlyOffline = isDeviceOffline()
        val isNewPlot = _uiState.value.plotId != plotId
        val wasOffline = if (isNewPlot) currentlyOffline else (_uiState.value.isOffline || currentlyOffline)

        if (!isNewPlot && _uiState.value.samples.isNotEmpty() && _uiState.value.submissionSummary != null && !wasOffline) {
            // Already active in this session and online with samples and summary loaded; avoid redundant fetch
            return
        }

        if (isNewPlot) {
            _uiState.value = SamplingSessionUiState(
                plotId = plotId,
                plotName = plotName,
                campaignYear = campaignYear,
                samples = emptyList(),
                targetTreesCount = 5,
                isLoading = !currentlyOffline,
                isOffline = currentlyOffline,
                hasRecoveredConnection = false,
            )
        } else {
            _uiState.update {
                it.copy(
                    isLoading = if (!currentlyOffline && it.submissionSummary == null) true else it.isLoading,
                    isOffline = currentlyOffline,
                    hasRecoveredConnection = false,
                )
            }
        }

        if (isNewPlot || wasOffline || _uiState.value.submissionSummary == null) {
            viewModelScope.launch {
                val drafts = draftDao.getSamples(plotId, campaignYear)
                if (drafts.isNotEmpty()) {
                    val domainSamples = drafts.map { it.toDomain() }
                    _uiState.update { it.copy(samples = domainSamples) }

                    val unsynced = drafts.filter { !it.isSynced }
                    if (unsynced.isNotEmpty()) {
                        if (isDeviceOffline()) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isOffline = true,
                                    hasRecoveredConnection = false,
                                )
                            }
                            return@launch
                        }

                        val batchId = UUID.randomUUID().toString()
                        submitSamplingBatch(
                            plotId = plotId,
                            campaignYear = campaignYear,
                            batchId = batchId,
                            samples = unsynced.map { it.toDomain() },
                        ).fold(
                            onSuccess = { summary ->
                                unsynced.forEach { draftDao.markSampleAsSynced(it.id) }
                                _uiState.update { current ->
                                    val updated = current.samples.map { sample ->
                                        if (unsynced.any { it.treeIdentifier == sample.treeIdentifier }) {
                                            sample.copy(isSynced = true)
                                        } else {
                                            sample
                                        }
                                    }
                                    current.copy(
                                        isLoading = false,
                                        samples = updated,
                                        submissionSummary = summary,
                                        isOffline = false,
                                        hasRecoveredConnection = wasOffline || unsynced.isNotEmpty(),
                                        syncedOnResumeCount = if (wasOffline || unsynced.isNotEmpty()) unsynced.size else 0,
                                    )
                                }
                            },
                            onFailure = { error ->
                                val isOffline = error is pe.edu.upc.viora.core.domain.AppError.Offline ||
                                    error is pe.edu.upc.viora.core.domain.AppError.Timeout
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isOffline = isOffline || it.isOffline,
                                        hasRecoveredConnection = false,
                                    )
                                }
                            },
                        )
                    } else {
                        if (!isDeviceOffline() && _uiState.value.submissionSummary == null) {
                            thinningRepository.getSamplingSummary(plotId, campaignYear).fold(
                                onSuccess = { summary ->
                                    _uiState.update { it.copy(isLoading = false, submissionSummary = summary) }
                                },
                                onFailure = {
                                    _uiState.update { it.copy(isLoading = false) }
                                },
                            )
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                } else if (!isDeviceOffline()) {
                    thinningRepository.getSamplingDetailed(plotId, campaignYear).fold(
                        onSuccess = { report ->
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
                                    samples = report.trees,
                                    submissionSummary = report.summary,
                                    targetTreesCount = (report.summary.evaluatedTreesCount + report.summary.treesNeeded).coerceAtLeast(5),
                                    isOffline = false,
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update { it.copy(isLoading = false) }
                        },
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun addSample(
        treeIdentifier: String,
        shootsCount: Int,
        fruitSetCount: Int,
        trunkCircumferenceCm: Double?,
    ) {
        val currentState = _uiState.value
        val sample = TreeSample(
            treeIdentifier = treeIdentifier.ifBlank { currentState.nextTreeIdentifier },
            shootsCount = shootsCount,
            fruitSetCount = fruitSetCount,
            trunkCircumferenceCm = trunkCircumferenceCm,
            observedOn = LocalDate.now(),
            isSynced = false,
        )
        _uiState.update { current ->
            current.copy(
                samples = current.samples + sample,
                hasRecoveredConnection = false,
            )
        }
        viewModelScope.launch {
            val entity = sample.toEntity(currentState.plotId, currentState.plotName, currentState.campaignYear)
            draftDao.insertSample(entity)

            if (isDeviceOffline()) {
                _uiState.update { it.copy(isOffline = true, hasRecoveredConnection = false) }
                return@launch
            }

            val batchId = UUID.randomUUID().toString()
            submitSamplingBatch(
                plotId = currentState.plotId,
                campaignYear = currentState.campaignYear,
                batchId = batchId,
                samples = listOf(sample),
            ).fold(
                onSuccess = { summary ->
                    draftDao.markSampleAsSynced(entity.id)
                    _uiState.update { current ->
                        val updated = current.samples.map {
                            if (it.treeIdentifier == sample.treeIdentifier) it.copy(isSynced = true) else it
                        }
                        current.copy(
                            samples = updated,
                            submissionSummary = summary,
                            isOffline = false,
                            hasRecoveredConnection = false,
                        )
                    }
                },
                onFailure = { error ->
                    val isOffline = error is pe.edu.upc.viora.core.domain.AppError.Offline ||
                        error is pe.edu.upc.viora.core.domain.AppError.Timeout
                    if (isOffline) {
                        _uiState.update { it.copy(isOffline = true, hasRecoveredConnection = false) }
                    }
                },
            )
        }
    }

    fun submitCurrentBatch(
        onSuccess: (SamplingSummary) -> Unit,
        onOffline: (() -> Unit)? = null,
    ) {
        val state = _uiState.value
        if (state.samples.isEmpty() || state.plotId.isBlank()) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }
        val batchId = UUID.randomUUID().toString()

        viewModelScope.launch {
            val unsyncedSamples = state.samples.filter { !it.isSynced }
            val samplesToSend = if (unsyncedSamples.isEmpty()) state.samples else unsyncedSamples

            submitSamplingBatch(
                plotId = state.plotId,
                campaignYear = state.campaignYear,
                batchId = batchId,
                samples = samplesToSend,
            ).fold(
                onSuccess = { summary ->
                    draftDao.clearSamples(state.plotId, state.campaignYear)
                    val hadRecovered = _uiState.value.isOffline
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionSummary = summary,
                            isOffline = false,
                            hasRecoveredConnection = hadRecovered,
                            error = null,
                        )
                    }
                    onSuccess(summary)
                },
                onFailure = { error ->
                    val isOfflineError = error == pe.edu.upc.viora.core.domain.AppError.Offline ||
                        error is pe.edu.upc.viora.core.domain.AppError.Timeout
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isOffline = isOfflineError || it.isOffline,
                            error = error,
                        )
                    }
                    if (isOfflineError && onOffline != null) {
                        onOffline()
                    }
                },
            )
        }
    }

    fun setOffline(isOffline: Boolean) {
        _uiState.update { it.copy(isOffline = isOffline) }
    }

    fun setRecoveredConnection(hasRecovered: Boolean) {
        _uiState.update { it.copy(hasRecoveredConnection = hasRecovered) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
