package pe.edu.upc.viora.features.telemetry.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity

/** Filter criteria for the capsules in the Alerts Center. */
enum class AlertsFilter {
    ALL,
    CRITICAL,
    WARNING,
    NORMALIZED,
}

/**
 * A plot in the plot filter sheet: its active alerts by severity, so the sheet can draw the dot of
 * the worst one and say how many there are.
 */
data class PlotFilterOption(
    val plotId: String,
    val plotName: String,
    val criticalCount: Int,
    val warningCount: Int,
) {
    val activeCount: Int get() = criticalCount + warningCount

    /** The severity of the dot: critical wins over warning; null when the plot has no active alert. */
    val worstSeverity: IncidentSeverity?
        get() = when {
            criticalCount > 0 -> IncidentSeverity.CRITICAL
            warningCount > 0 -> IncidentSeverity.WARNING
            else -> null
        }
}

/** UI state for the central alerts inboxs. */
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
        val affectedPlotNames: List<String> = emptyList(),
        val latestTriggeredAt: String? = null,
        /** Plots that have alerts, for the plot filter sheet. */
        val plotOptions: List<PlotFilterOption> = emptyList(),
        /** The plot the screen is filtered by, or null for every plot. */
        val selectedPlotId: String? = null,
    ) : AlertsUiState {
        /** The filter button only shows when there is more than one plot to choose, or a filter to clear. */
        val canFilterByPlot: Boolean get() = plotOptions.size > 1 || selectedPlotId != null
    }

    data class Empty(
        val summary: AlertsSummary = AlertsSummary(),
        val activeFilter: AlertsFilter = AlertsFilter.ALL,
    ) : AlertsUiState
}
