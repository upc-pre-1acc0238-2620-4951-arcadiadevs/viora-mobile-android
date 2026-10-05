package pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.croploadregulation.presentation.state.SamplingSessionUiState

@HiltViewModel
class SamplingSessionViewModel @Inject constructor(
    private val submitSamplingBatch: SubmitSamplingBatchUseCase,
    private val draftDao: DraftTreeSampleDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SamplingSessionUiState())
    val uiState: StateFlow<SamplingSessionUiState> = _uiState.asStateFlow()

    fun initSession(plotId: String, plotName: String, campaignYear: Int = java.time.Year.now().value) {
        if (_uiState.value.plotId != plotId) {
            _uiState.value = SamplingSessionUiState(
                plotId = plotId,
                plotName = plotName,
                campaignYear = campaignYear,
                samples = emptyList(),
                targetTreesCount = 5,
            )
            viewModelScope.launch {
                val drafts = draftDao.getSamples(plotId, campaignYear).map { it.toDomain() }
                if (drafts.isNotEmpty()) {
                    _uiState.update { it.copy(samples = drafts) }
                }
            }
        } else if (_uiState.value.samples.isEmpty()) {
            viewModelScope.launch {
                val drafts = draftDao.getSamples(plotId, campaignYear).map { it.toDomain() }
                if (drafts.isNotEmpty()) {
                    _uiState.update { it.copy(samples = drafts) }
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
            current.copy(samples = current.samples + sample)
        }
        viewModelScope.launch {
            val entity = sample.toEntity(currentState.plotId, currentState.plotName, currentState.campaignYear)
            draftDao.insertSample(entity)

            val batchId = "batch-${LocalDate.now()}-${UUID.randomUUID().toString().take(6)}"
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
                        )
                    }
                },
                onFailure = { error ->
                    val isOffline = error is pe.edu.upc.viora.core.domain.AppError.Offline ||
                        error is pe.edu.upc.viora.core.domain.AppError.Timeout
                    if (isOffline) {
                        _uiState.update { it.copy(isOffline = true) }
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
        val batchId = "batch-${LocalDate.now()}-${UUID.randomUUID().toString().take(6)}"

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
