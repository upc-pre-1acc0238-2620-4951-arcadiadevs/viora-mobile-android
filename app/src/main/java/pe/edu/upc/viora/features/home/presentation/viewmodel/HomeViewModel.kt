package pe.edu.upc.viora.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.PlotId
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.features.home.application.usecase.ChoosePlotUseCase
import pe.edu.upc.viora.features.home.application.usecase.ObserveChosenPlotUseCase
import pe.edu.upc.viora.features.home.presentation.state.HomeUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.PickFocusedPlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.published.Plot
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshIncidentsUseCase

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
    observeChosenPlot: ObserveChosenPlotUseCase,
    private val choosePlot: ChoosePlotUseCase,
    pickFocusedPlot: PickFocusedPlotUseCase,
) : ViewModel() {

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    /** The plot the cards of the Home talk about: the chosen one, else the biggest. */
    private val focusedPlot: Flow<Plot?> = combine(observePlots(), observeChosenPlot()) { plots, chosenId ->
        pickFocusedPlot(plots, chosenId)
    }.distinctUntilChanged()

    /** Active alerts of every plot (T14 and the Home card count them all), so none hides behind the focused plot. */
    private val activeAlertsCount: Flow<Long> = observeIncidents(null).map { incidents ->
        incidents.count { it.status.isActive }.toLong()
    }

    val uiState: StateFlow<HomeUiState> = combine(
        observePlots(),
        observeLastRefresh(),
        activeAlertsCount,
        refreshState,
        focusedPlot,
    ) { plots, lastRefresh, activeCount, refresh, focused ->
        val offline = refresh.error?.isConnectivity == true

        when {
            plots.isNotEmpty() -> HomeUiState.Content(
                plots = plots,
                isOffline = offline,
                lastRefresh = lastRefresh,
                isRefreshing = refresh.isRefreshing,
                activeAlertsCount = activeCount,
                focusedPlot = focused,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> HomeUiState.Loading
            refresh.error != null -> HomeUiState.Error(refresh.error)
            else -> HomeUiState.NoPlots(
                isOffline = offline,
                lastRefresh = lastRefresh,
                activeAlertsCount = activeCount,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState.Loading)

    init {
        // The incidents of every plot come with refresh(); the harvests of the plot in focus are refreshed by the
        // alternation card that phenology draws for it.
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

    /** The producer picked the plot the Home should talk about. */
    fun focusPlot(plotId: PlotId) {
        viewModelScope.launch { choosePlot(plotId.value) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
