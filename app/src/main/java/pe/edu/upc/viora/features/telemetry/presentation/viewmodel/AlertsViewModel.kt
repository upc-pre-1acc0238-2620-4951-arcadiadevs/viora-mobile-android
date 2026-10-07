package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsLastRefreshUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsFilter
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsUiState
import pe.edu.upc.viora.features.telemetry.presentation.state.PlotFilterOption

/**
 * ViewModel for the Alerts Center (T14).
 * Always observes and downloads the alerts of every plot, so none hides; the producer can narrow the
 * screen to one plot (plot filter sheet) and, on top of that, by severity (the capsules). The
 * `plotId` navigation argument only preselects the plot filter.
 */
@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observeIncidents: ObserveIncidentsUseCase,
    private val refreshIncidents: RefreshIncidentsUseCase,
    observeLastRefresh: ObserveIncidentsLastRefreshUseCase? = null,
) : ViewModel() {

    private val selectedPlotId = savedStateHandle.getStateFlow<String?>(SELECTED_PLOT_KEY, savedStateHandle.get<String>("plotId"))

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val currentFilter = MutableStateFlow(AlertsFilter.ALL)
    private val summaryState = MutableStateFlow(AlertsSummary())
    private val refreshState = MutableStateFlow(RefreshState())

    private val filters = combine(currentFilter, selectedPlotId) { severity, plot -> severity to plot }

    val uiState: StateFlow<AlertsUiState> = combine(
        observeIncidents(null),
        filters,
        summaryState,
        refreshState,
        observeLastRefresh?.invoke(null) ?: flowOf(null),
    ) { allIncidents, (filter, chosenPlotId), summary, refresh, lastRefresh ->
        val plotOptions = plotOptionsOf(allIncidents)
        // A plot whose alerts are gone falls back to every plot instead of an empty screen.
        val plot = plotOptions.firstOrNull { it.plotId == chosenPlotId }
        val scoped = if (plot == null) allIncidents else allIncidents.filter { it.plotId == plot.plotId }

        // The summary of the server covers every plot: with a plot chosen, count its alerts here.
        val serverHasSummary = summary.activeCount > 0 || summary.criticalCount > 0 || summary.warningCount > 0 || summary.normalizedCount > 0
        val resolvedSummary = if (plot == null && serverHasSummary) summary else summaryOf(scoped)

        val filteredList = when (filter) {
            AlertsFilter.ALL -> scoped
            AlertsFilter.CRITICAL -> scoped.filter { it.severity == IncidentSeverity.CRITICAL && it.status != IncidentStatus.NORMALIZED }
            AlertsFilter.WARNING -> scoped.filter { it.severity == IncidentSeverity.WARNING && it.status != IncidentStatus.NORMALIZED }
            AlertsFilter.NORMALIZED -> scoped.filter { it.status == IncidentStatus.NORMALIZED }
        }

        val activeIncidents = scoped.filter { it.status != IncidentStatus.NORMALIZED }
        val affectedPlotNames = if (plot != null) {
            listOf(plot.plotName)
        } else {
            activeIncidents.map { it.plotName }.filter { it.isNotBlank() }.distinct()
        }

        val latestTrigger = activeIncidents.map { it.triggeredAt }.filter { it.isNotBlank() }.maxOrNull()

        val fallbackSummary = affectedPlotNames.joinToString(", ")

        when {
            allIncidents.isNotEmpty() || filter != AlertsFilter.ALL -> AlertsUiState.Content(
                incidents = filteredList,
                summary = resolvedSummary,
                activeFilter = filter,
                isRefreshing = refresh.isRefreshing,
                refreshError = refresh.error,
                affectedPlotsSummary = fallbackSummary,
                affectedPlotNames = affectedPlotNames,
                latestTriggeredAt = latestTrigger,
                plotOptions = plotOptions,
                selectedPlotId = plot?.plotId,
            )
            // Incidents downloaded before and none cached: the plot has no alerts, say so at once.
            lastRefresh == null && (refresh.isRefreshing || !refresh.hasFinishedOnce) -> AlertsUiState.Loading
            refresh.error != null && lastRefresh == null -> AlertsUiState.Error(refresh.error)
            else -> AlertsUiState.Empty(summary = resolvedSummary, activeFilter = filter)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AlertsUiState.Loading)

    init {
        refresh()
    }

    fun setFilter(filter: AlertsFilter) {
        currentFilter.update { current ->
            if (current == filter) AlertsFilter.ALL else filter
        }
    }

    /** Narrows the screen to one plot, or to every plot with null. Survives rotation and process death. */
    fun selectPlot(plotId: String?) {
        savedStateHandle[SELECTED_PLOT_KEY] = plotId
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = refreshIncidents(plotId = null)
            when (result) {
                is AppResult.Success -> {
                    summaryState.value = result.value
                    refreshState.update {
                        RefreshState(
                            isRefreshing = false,
                            hasFinishedOnce = true,
                            error = null,
                        )
                    }
                }
                is AppResult.Failure -> {
                    refreshState.update {
                        RefreshState(
                            isRefreshing = false,
                            hasFinishedOnce = true,
                            error = result.error,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val SELECTED_PLOT_KEY = "selectedPlotId"
    }
}

/** Plots that have alerts (the ones without a plot only show under every plot), worst first, then by name. */
internal fun plotOptionsOf(incidents: List<AgroclimaticIncident>): List<PlotFilterOption> =
    incidents
        .filter { it.plotId.isNotBlank() }
        .groupBy { it.plotId }
        .map { (plotId, alerts) ->
            val active = alerts.filter { it.status != IncidentStatus.NORMALIZED }
            PlotFilterOption(
                plotId = plotId,
                plotName = alerts.firstOrNull { it.plotName.isNotBlank() }?.plotName ?: plotId,
                criticalCount = active.count { it.severity == IncidentSeverity.CRITICAL },
                warningCount = active.count { it.severity == IncidentSeverity.WARNING },
            )
        }
        .sortedWith(compareBy({ it.worstSeverity?.ordinal ?: Int.MAX_VALUE }, { it.plotName }))

private fun summaryOf(incidents: List<AgroclimaticIncident>): AlertsSummary {
    val active = incidents.filter { it.status != IncidentStatus.NORMALIZED }
    val critical = active.count { it.severity == IncidentSeverity.CRITICAL }.toLong()
    val warning = active.count { it.severity == IncidentSeverity.WARNING }.toLong()
    return AlertsSummary(
        activeCount = critical + warning,
        criticalCount = critical,
        warningCount = warning,
        normalizedCount = incidents.count { it.status == IncidentStatus.NORMALIZED }.toLong(),
    )
}
