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
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsFilter
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsUiState

/**
 * ViewModel for the Alerts Center.
 * Combines cached incidents with interactive filter capsules (Critical, Warning, Normalized).
 */
@HiltViewModel
class AlertsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeIncidents: ObserveIncidentsUseCase,
    private val refreshIncidents: RefreshIncidentsUseCase,
    observeLastRefresh: ObserveIncidentsLastRefreshUseCase? = null,
) : ViewModel() {

    val plotId: String? = savedStateHandle.get<String>("plotId")

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val currentFilter = MutableStateFlow(AlertsFilter.ALL)
    private val summaryState = MutableStateFlow(AlertsSummary())
    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<AlertsUiState> = combine(
        observeIncidents(plotId),
        currentFilter,
        summaryState,
        refreshState,
        observeLastRefresh?.invoke(plotId) ?: flowOf(null),
    ) { allIncidents, filter, summary, refresh, lastRefresh ->
        val resolvedSummary = if (summary.activeCount > 0 || summary.criticalCount > 0 || summary.warningCount > 0 || summary.normalizedCount > 0) {
            summary
        } else {
            val critical = allIncidents.count { it.severity == IncidentSeverity.CRITICAL && it.status != IncidentStatus.NORMALIZED }.toLong()
            val warning = allIncidents.count { it.severity == IncidentSeverity.WARNING && it.status != IncidentStatus.NORMALIZED }.toLong()
            val normalized = allIncidents.count { it.status == IncidentStatus.NORMALIZED }.toLong()
            AlertsSummary(
                activeCount = critical + warning,
                criticalCount = critical,
                warningCount = warning,
                normalizedCount = normalized,
            )
        }

        val filteredList = when (filter) {
            AlertsFilter.ALL -> allIncidents
            AlertsFilter.CRITICAL -> allIncidents.filter { it.severity == IncidentSeverity.CRITICAL && it.status != IncidentStatus.NORMALIZED }
            AlertsFilter.WARNING -> allIncidents.filter { it.severity == IncidentSeverity.WARNING && it.status != IncidentStatus.NORMALIZED }
            AlertsFilter.NORMALIZED -> allIncidents.filter { it.status == IncidentStatus.NORMALIZED }
        }

        val activeIncidents = allIncidents.filter { it.status != IncidentStatus.NORMALIZED }
        val affectedPlotNames = activeIncidents
            .map { it.plotName }
            .filter { it.isNotBlank() }
            .distinct()

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

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = refreshIncidents(plotId = plotId)
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
    }
}
