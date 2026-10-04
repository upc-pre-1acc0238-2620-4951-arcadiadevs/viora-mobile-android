package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.application.usecase.CompleteMitigationStepUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.GetIncidentDetailUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.PostponeIncidentUseCase
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertDetailUiState

/**
 * ViewModel for the Alert Detail screen.
 * Coordinates incident detail retrieval, task completion, and postponement (snooze).
 */
@HiltViewModel
class AlertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getIncidentDetail: GetIncidentDetailUseCase,
    private val postponeIncidentUseCase: PostponeIncidentUseCase,
    private val completeMitigationStepUseCase: CompleteMitigationStepUseCase,
) : ViewModel() {

    val incidentId: String = checkNotNull(savedStateHandle.get<String>("incidentId")) {
        "AlertDetailRoute requires an incidentId"
    }

    private val _uiState = MutableStateFlow<AlertDetailUiState>(AlertDetailUiState.Loading)
    val uiState: StateFlow<AlertDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        _uiState.value = AlertDetailUiState.Loading
        viewModelScope.launch {
            when (val result = getIncidentDetail(incidentId)) {
                is AppResult.Success -> {
                    _uiState.value = AlertDetailUiState.Content(detail = result.value)
                }
                is AppResult.Failure -> {
                    _uiState.value = AlertDetailUiState.Error(error = result.error)
                }
            }
        }
    }

    fun completeStep(stepId: String) {
        val currentState = _uiState.value as? AlertDetailUiState.Content ?: return
        val currentDetail = currentState.detail
        val step = currentDetail.mitigationSteps.find { it.id == stepId } ?: return
        if (step.completed) return

        val updatedSteps = currentDetail.mitigationSteps.map {
            if (it.id == stepId) it.copy(completed = true) else it
        }
        _uiState.value = currentState.copy(detail = currentDetail.copy(mitigationSteps = updatedSteps))

        viewModelScope.launch {
            val result = completeMitigationStepUseCase(incidentId = incidentId, stepId = stepId)
            if (result is AppResult.Failure) {
                _uiState.value = currentState.copy(
                    detail = currentDetail,
                    userFeedbackMessage = "rollback",
                )
            }
        }
    }

    fun postpone(durationHours: Int) {
        val currentState = _uiState.value as? AlertDetailUiState.Content ?: return
        _uiState.value = currentState.copy(isSnoozing = true)

        viewModelScope.launch {
            val result = postponeIncidentUseCase(incidentId = incidentId, durationHours = durationHours)
            when (result) {
                is AppResult.Success -> {
                    _uiState.value = currentState.copy(
                        isSnoozing = false,
                        detail = currentState.detail.copy(status = IncidentStatus.SNOOZED),
                        userFeedbackMessage = "snoozed",
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = currentState.copy(
                        isSnoozing = false,
                        userFeedbackMessage = "snooze_error",
                    )
                }
            }
        }
    }

    fun clearFeedbackMessage() {
        val currentState = _uiState.value as? AlertDetailUiState.Content ?: return
        if (currentState.userFeedbackMessage != null) {
            _uiState.value = currentState.copy(userFeedbackMessage = null)
        }
    }
}
