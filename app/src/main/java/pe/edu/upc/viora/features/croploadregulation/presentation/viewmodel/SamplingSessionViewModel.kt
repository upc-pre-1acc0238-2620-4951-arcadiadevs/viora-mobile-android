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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.fold
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SubmitSamplingBatchUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleEntity
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

    /** Serializes the sends, so a resend on opening the screen never races a tree being added. */
    private val sendLock = Mutex()
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

                        val outcome = sendDrafts(unsynced)
                        _uiState.update { current ->
                            val synced = outcome.syncedTrees
                            val updated = current.samples
                                .filterNot { it.treeIdentifier in outcome.droppedTrees }
                                .map { if (it.treeIdentifier in synced) it.copy(isSynced = true) else it }
                            current.copy(
                                isLoading = false,
                                samples = updated,
                                submissionSummary = outcome.summary ?: current.submissionSummary,
                                isOffline = outcome.offline || (outcome.summary == null && current.isOffline),
                                hasRecoveredConnection = synced.isNotEmpty(),
                                syncedOnResumeCount = synced.size,
                                rejectedMessage = outcome.rejectedMessage ?: current.rejectedMessage,
                            )
                        }
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
            val entity = sample.toEntity(currentState.plotId, currentState.plotName, currentState.campaignYear,
                batchId = UUID.randomUUID().toString())
            draftDao.insertSample(entity)

            if (isDeviceOffline()) {
                _uiState.update { it.copy(isOffline = true, hasRecoveredConnection = false) }
                return@launch
            }

            val outcome = sendDrafts(listOf(entity))
            _uiState.update { current ->
                current.copy(
                    samples = current.samples
                        .filterNot { it.treeIdentifier in outcome.droppedTrees }
                        .map { if (it.treeIdentifier in outcome.syncedTrees) it.copy(isSynced = true) else it },
                    submissionSummary = outcome.summary ?: current.submissionSummary,
                    isOffline = outcome.offline || (outcome.summary == null && current.isOffline),
                    hasRecoveredConnection = false,
                    rejectedMessage = outcome.rejectedMessage ?: current.rejectedMessage,
                )
            }
        }
    }

    fun submitCurrentBatch(
        onSuccess: (SamplingSummary) -> Unit,
        onOffline: (() -> Unit)? = null,
    ) {
        val state = _uiState.value
        if (state.samples.isEmpty() || state.plotId.isBlank()) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            // Only the trees the server has not taken yet are sent, each with its own batch id: what is already
            // synced is never sent again, so no round is counted twice.
            val unsynced = draftDao.getSamples(state.plotId, state.campaignYear).filter { !it.isSynced }
            val outcome = sendDrafts(unsynced)
            val result = when {
                outcome.offline -> null
                unsynced.isEmpty() || outcome.summary == null ->
                    thinningRepository.getSamplingSummary(state.plotId, state.campaignYear)
                else -> AppResult.Success(outcome.summary)
            }
            when {
                result is AppResult.Success -> {
                    draftDao.clearSamples(state.plotId, state.campaignYear)
                    val hadRecovered = _uiState.value.isOffline
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionSummary = result.value,
                            isOffline = false,
                            hasRecoveredConnection = hadRecovered,
                            error = null,
                            rejectedMessage = outcome.rejectedMessage ?: it.rejectedMessage,
                        )
                    }
                    onSuccess(result.value)
                }
                else -> {
                    val error = (result as? AppResult.Failure)?.error ?: AppError.Offline
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isOffline = error.isConnectivity || it.isOffline,
                            error = error,
                            rejectedMessage = outcome.rejectedMessage ?: it.rejectedMessage,
                        )
                    }
                    if (error.isConnectivity && onOffline != null) {
                        onOffline()
                    }
                }
            }
        }
    }

    /** What sending some drafts left behind. */
    private data class SendOutcome(
        val syncedTrees: Set<String> = emptySet(),
        val droppedTrees: Set<String> = emptySet(),
        val summary: SamplingSummary? = null,
        val offline: Boolean = false,
        val rejectedMessage: String? = null,
    )

    /**
     * Sends drafts one at a time, each with the batch id it keeps, under one lock so two sends never overlap. A draft
     * the server takes is marked synced; one it refuses for good (409, such as an already issued prescription) is
     * dropped and its reason kept; losing the connection stops the rest, which stay for the next attempt.
     */
    private suspend fun sendDrafts(drafts: List<DraftTreeSampleEntity>): SendOutcome = sendLock.withLock {
        var outcome = SendOutcome()
        for (draft in drafts) {
            val batchId = draft.batchId ?: UUID.randomUUID().toString().also { draftDao.setBatchId(draft.id, it) }
            when (val result = submitSamplingBatch(draft.plotId, draft.campaignYear, batchId, listOf(draft.toDomain()))) {
                is AppResult.Success -> {
                    draftDao.markSampleAsSynced(draft.id)
                    outcome = outcome.copy(syncedTrees = outcome.syncedTrees + draft.treeIdentifier, summary = result.value)
                }
                is AppResult.Failure -> when {
                    result.error.isConnectivity -> return@withLock outcome.copy(offline = true)
                    result.error is AppError.Conflict -> {
                        draftDao.deleteSample(draft.id)
                        outcome = outcome.copy(
                            droppedTrees = outcome.droppedTrees + draft.treeIdentifier,
                            rejectedMessage = (result.error as AppError.Conflict).detail ?: outcome.rejectedMessage,
                        )
                    }
                    // Any other failure keeps the draft, to be sent again later with the same batch id
                    else -> return@withLock outcome
                }
            }
        }
        outcome
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
