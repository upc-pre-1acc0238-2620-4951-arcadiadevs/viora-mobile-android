package pe.edu.upc.viora.features.telemetry.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary

enum class AlertsFilter {
    ALL,
    CRITICAL,
    WARNING,
    NORMALIZED,
}

sealed interface AlertsUiState {
    data object Loading : AlertsUiState

    data class Error(val error: AppError) : AlertsUiState

    data class Content(
        val incidents: List<AgroclimaticIncident>,
        val summary: AlertsSummary,
        val activeFilter: AlertsFilter = AlertsFilter.ALL,
        val isRefreshing: Boolean = false,
        val refreshError: AppError? = null,
        val affectedPlotsSummary: String = "",
    ) : AlertsUiState

    data class Empty(
        val summary: AlertsSummary = AlertsSummary(),
        val activeFilter: AlertsFilter = AlertsFilter.ALL,
    ) : AlertsUiState
}
