package pe.edu.upc.viora.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
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
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.croploadregulation.application.published.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveCampaignPlansUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RefreshCampaignPlansUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.published.PlotToSettle
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePlotsToSettleUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.home.application.usecase.ObserveChosenPlotUseCase
import pe.edu.upc.viora.features.home.presentation.state.HarvestProgress
import pe.edu.upc.viora.features.home.presentation.state.HomePhaseRules
import pe.edu.upc.viora.features.home.presentation.state.HomePhaseUiState
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveCurrentPhaseUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.published.Plot
import pe.edu.upc.viora.features.plotmanagement.application.usecase.PickFocusedPlotUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveForecastUseCase
import pe.edu.upc.viora.features.telemetry.application.published.WeatherForecast

/**
 * Feeds the Home phase carousel (Figma P10 · Fase) for the plot in focus: its thinning plan, its winter chill and
 * its forecast, plus the plots still to register for the harvest card and its picker (P70). The forecast is
 * downloaded by the "Hoy en tu campo" weather card; this one only reads the cache.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomePhaseViewModel @Inject constructor(
    observePlots: ObservePlotsUseCase,
    observeChosenPlot: ObserveChosenPlotUseCase,
    observeCampaignPlans: ObserveCampaignPlansUseCase,
    private val refreshCampaignPlans: RefreshCampaignPlansUseCase,
    private val refreshChill: RefreshChillTrackerUseCase,
    private val observeForecast: ObserveForecastUseCase,
    observePlotsToSettle: ObservePlotsToSettleUseCase,
    private val refreshSettlements: RefreshSettledHarvestsUseCase,
    private val clock: Clock,
    pickFocusedPlot: PickFocusedPlotUseCase,
    private val observeCurrentPhase: ObserveCurrentPhaseUseCase,
) : ViewModel() {

    private val plots = observePlots()

    private val focused: Flow<Plot?> = combine(plots, observeChosenPlot()) { plots, chosen -> pickFocusedPlot(plots, chosen) }
        .distinctUntilChanged()

    /** False until the settlements were refreshed once, so the harvest card does not count plots already settled. */
    private val settlementsReady = MutableStateFlow(false)

    private val harvest: Flow<HarvestProgress?> =
        combine(plots, observePlotsToSettle(plots.map { it.toSettleCandidates() }), settlementsReady) { plots, toSettle, ready ->
            if (!ready) null else HarvestProgress(today().year, toSettle, plots.size)
        }

    private data class PlotData(val plot: Plot?, val forecast: WeatherForecast?, val plan: CampaignPlan?, val harvest: HarvestProgress?)

    private val plotData: Flow<PlotData> = combine(
        focused.flatMapLatest { plot ->
            if (plot == null) flowOf<Pair<Plot?, WeatherForecast?>>(null to null) else observeForecast(plot.id.value).map { plot to it }
        },
        observeCampaignPlans(today().year),
        harvest,
    ) { (plot, forecast), plans, harvest ->
        PlotData(plot, forecast, plans.firstOrNull { it.plotId == plot?.id?.value }, harvest)
    }

    /** Phenology says which phase the plot is in; the cards only put it into words. */
    val uiState: StateFlow<HomePhaseUiState> = plotData.flatMapLatest { data ->
        observeCurrentPhase(data.plot?.id?.value, HomePhaseRules.phaseDates(data.plan, data.harvest)).map { phase ->
            HomePhaseRules.state(phase, data.plot, data.plan, data.forecast, data.harvest)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomePhaseUiState())

    init {
        viewModelScope.launch { refreshCampaignPlans(today().year) }
        viewModelScope.launch {
            focused.map { it?.id?.value }.distinctUntilChanged().collect { id -> if (id != null) refreshChill(id) }
        }
        viewModelScope.launch {
            plots.map { list -> list.map { it.id.value } }.distinctUntilChanged().collect { ids ->
                if (ids.isNotEmpty()) {
                    refreshSettlements(ids)
                    settlementsReady.value = true
                }
            }
        }
    }

    private fun today(): LocalDate = LocalDate.now(clock)

    /** The active plots as candidates to settle; the use case keeps those still to register. */
    private fun List<Plot>.toSettleCandidates(): List<PlotToSettle> =
        filter { it.isActive }.map { PlotToSettle(it.id.value, it.name, it.variety, it.areaHectares) }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
