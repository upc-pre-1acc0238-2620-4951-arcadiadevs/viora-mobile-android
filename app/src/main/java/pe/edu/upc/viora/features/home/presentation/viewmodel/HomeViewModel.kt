package pe.edu.upc.viora.features.home.presentation.viewmodel

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
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.home.presentation.state.HomeUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase

/**
 * Home (P10). Observes plots and agroclimatic incidents for unified status and alerts badge.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    observePlots: ObservePlotsUseCase,
    observeLastRefresh: ObservePlotsLastRefreshUseCase,
    observeIncidents: ObserveIncidentsUseCase,
    private val refreshPlots: RefreshPlotsUseCase,
    private val refreshIncidents: RefreshIncidentsUseCase,
) : ViewModel() {

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<HomeUiState> = combine(
        observePlots(),
        observeLastRefresh(),
        observeIncidents(null),
        refreshState,
    ) { plots, lastRefresh, incidents, refresh ->
        val offline = refresh.error == AppError.Offline
        val activeCount = incidents.count {
            it.status != IncidentStatus.NORMALIZED
        }.toLong()

        when {
            plots.isNotEmpty() -> HomeUiState.Content(
                plots = plots,
                isOffline = offline,
                lastRefresh = lastRefresh,
                isRefreshing = refresh.isRefreshing,
                activeAlertsCount = activeCount,
            )
            refresh.error != null -> HomeUiState.Error(refresh.error)
            else -> HomeUiState.NoPlots(
                isOffline = offline,
                lastRefresh = lastRefresh,
                activeAlertsCount = activeCount,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val plotsResult = refreshPlots()
            refreshIncidents()
            refreshState.update {
                RefreshState(
                    isRefreshing = false,
                    hasFinishedOnce = true,
                    error = (plotsResult as? AppResult.Failure)?.error,
                )
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
