package pe.edu.upc.viora.features.telemetry.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail

/** UI state for the deep alert detail screen with weekly trend and mitigation tasks. */
sealed interface AlertDetailUiState {
    data object Loading : AlertDetailUiState

    data class Error(val error: AppError) : AlertDetailUiState

    data class Content(
        val detail: IncidentDetail,
        val isSnoozing: Boolean = false,
        val userFeedbackMessage: String? = null,
    ) : AlertDetailUiState
}
