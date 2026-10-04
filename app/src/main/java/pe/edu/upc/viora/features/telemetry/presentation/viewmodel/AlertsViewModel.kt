package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsFilter
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsUiState

@HiltViewModel
class AlertsViewModel @Inject constructor(
    observeIncidents: ObserveIncidentsUseCase,
    private val refreshIncidents: RefreshIncidentsUseCase,
) : ViewModel() {

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val currentFilter = MutableStateFlow(AlertsFilter.ALL)
    private val summaryState = MutableStateFlow(AlertsSummary())
    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<AlertsUiState> = combine(
        observeIncidents(null),
        currentFilter,
        summaryState,
        refreshState,
    ) { allIncidents, filter, summary, refresh ->
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

        val affectedPlots = allIncidents
            .filter { it.status != IncidentStatus.NORMALIZED }
            .map { it.plotName }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = ", ")

        when {
            allIncidents.isNotEmpty() || filter != AlertsFilter.ALL -> AlertsUiState.Content(
                incidents = filteredList,
                summary = resolvedSummary,
                activeFilter = filter,
                isRefreshing = refresh.isRefreshing,
                refreshError = refresh.error,
                affectedPlotsSummary = affectedPlots,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> AlertsUiState.Loading
            refresh.error != null -> AlertsUiState.Error(refresh.error)
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
            val result = refreshIncidents()
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
