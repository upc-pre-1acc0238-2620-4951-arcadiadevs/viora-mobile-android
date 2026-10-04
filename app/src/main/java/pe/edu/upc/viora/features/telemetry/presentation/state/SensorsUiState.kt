package pe.edu.upc.viora.features.telemetry.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode

/** What the sensors list screen shows (US14). */
sealed interface SensorsUiState {

    /** First download in progress and nothing cached. */
    data object Loading : SensorsUiState

    /** Download failed and there is nothing cached. */
    data class Error(val error: AppError) : SensorsUiState

    /** Cached nodes available (may be refreshing in the background). */
    data class Content(
        val plotName: String,
        val nodes: List<SensorNode>,
        val isRefreshing: Boolean,
        val refreshError: AppError?,
    ) : SensorsUiState

    /** The plot has no sensor nodes and there is no download error. */
    data class Empty(
        val plotName: String,
    ) : SensorsUiState
}
