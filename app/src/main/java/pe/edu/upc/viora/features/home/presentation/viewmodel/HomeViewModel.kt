package pe.edu.upc.viora.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.home.application.usecase.ChoosePlotUseCase
import pe.edu.upc.viora.features.home.application.usecase.ObserveChosenPlotUseCase
import pe.edu.upc.viora.features.home.domain.FocusedPlotRule
import pe.edu.upc.viora.features.home.presentation.state.HomeAlternation
import pe.edu.upc.viora.features.home.presentation.state.HomeUiState
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveBearingIndexUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestLastRefreshUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestPresenter
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus

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
    private val observeHarvests: ObserveHarvestHistoryUseCase,
    private val observeBearingIndex: ObserveBearingIndexUseCase,
    private val observeHarvestRefresh: ObserveHarvestLastRefreshUseCase,
    private val refreshHarvests: RefreshHarvestHistoryUseCase,
    private val clock: Clock,
) : ViewModel() {

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    /** The plot the cards of the Home talk about: the chosen one, else the biggest. */
    private val focusedPlot: Flow<Plot?> = combine(observePlots(), observeChosenPlot()) { plots, chosenId ->
        FocusedPlotRule.pick(plots, chosenId)
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val alternation: Flow<HomeAlternation?> = focusedPlot.flatMapLatest { plot ->
        if (plot == null) {
            flowOf(null)
        } else {
            combine(
                observeHarvests(plot.id.value),
                observeBearingIndex(plot.id.value),
                observeHarvestRefresh(plot.id.value),
            ) { records, index, lastRefresh -> alternationOf(plot, records, index?.value, lastRefresh) }
        }
    }

    private val activeAlertsCount: Flow<Long> = observeIncidents(null).map { incidents ->
        incidents.count { it.status != IncidentStatus.NORMALIZED }.toLong()
    }

    private val focusAndAlternation: Flow<Pair<Plot?, HomeAlternation?>> =
        combine(focusedPlot, alternation) { focused, alternation -> focused to alternation }

    val uiState: StateFlow<HomeUiState> = combine(
        observePlots(),
        observeLastRefresh(),
        activeAlertsCount,
        refreshState,
        focusAndAlternation,
    ) { plots, lastRefresh, activeCount, refresh, (focused, alternation) ->
        val offline = refresh.error == AppError.Offline

        when {
            plots.isNotEmpty() -> HomeUiState.Content(
                plots = plots,
                isOffline = offline,
                lastRefresh = lastRefresh,
                isRefreshing = refresh.isRefreshing,
                activeAlertsCount = activeCount,
                focusedPlot = focused,
                alternation = alternation,
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
        refresh()
        // Whenever the plot in focus changes, bring its harvests up to date (best effort).
        viewModelScope.launch {
            focusedPlot.map { it?.id?.value }.distinctUntilChanged().collect { id ->
                if (id != null) refreshHarvests(id)
            }
        }
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

    private fun alternationOf(plot: Plot, records: List<HarvestRecord>, serverIndex: Double?, lastRefresh: Instant?): HomeAlternation? {
        // Nothing cached and never downloaded: say nothing instead of a wrong "you miss 3 campaigns".
        if (records.isEmpty() && lastRefresh == null) return null
        if (records.size < HoblynBbi.MIN_CAMPAIGNS) return HomeAlternation.Insufficient(HoblynBbi.MIN_CAMPAIGNS - records.size)
        val index = serverIndex ?: HoblynBbi.index(records) ?: return null
        val bbiClass = BbiClass.of(index)
        val summary = HarvestPresenter.summarize(records, bbiClass, LocalDate.now(clock).year)
        return HomeAlternation.Ready(
            records = records.sortedBy { it.campaignYear }.takeLast(LAST_CAMPAIGNS),
            areaHectares = plot.areaHectares,
            voice = summary.voice,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val LAST_CAMPAIGNS = 5
    }
}
