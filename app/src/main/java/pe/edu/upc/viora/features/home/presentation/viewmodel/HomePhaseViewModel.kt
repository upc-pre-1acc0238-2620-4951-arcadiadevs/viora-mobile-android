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
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveCampaignPlansUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RefreshCampaignPlansUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePendingSettlementsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.HarvestEntryRules
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.HarvestEntryUiState
import pe.edu.upc.viora.features.home.application.usecase.ObserveChosenPlotUseCase
import pe.edu.upc.viora.features.home.domain.FocusedPlotRule
import pe.edu.upc.viora.features.home.presentation.state.HomePhaseRules
import pe.edu.upc.viora.features.home.presentation.state.HomePhaseUiState
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveForecastUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast

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
    private val observeChill: ObserveChillTrackerUseCase,
    private val refreshChill: RefreshChillTrackerUseCase,
    private val observeForecast: ObserveForecastUseCase,
    observeSettlements: ObserveSettledHarvestsUseCase,
    observePending: ObservePendingSettlementsUseCase,
    private val refreshSettlements: RefreshSettledHarvestsUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val plots = observePlots()

    private val focused: Flow<Plot?> = combine(plots, observeChosenPlot()) { plots, chosen -> FocusedPlotRule.pick(plots, chosen) }
        .distinctUntilChanged()

    /** False until the settlements were refreshed once, so the harvest card does not count plots already settled. */
    private val settlementsReady = MutableStateFlow(false)

    private val harvest: Flow<HarvestEntryUiState?> =
        combine(plots, observeSettlements(), observePending(), settlementsReady) { plots, settlements, pending, ready ->
            val year = today().year
            if (!ready) null else HarvestEntryUiState(year, HarvestEntryRules.plotsToSettle(plots, settlements, pending, year), plots.size)
        }

    private data class PlotData(val plot: Plot?, val chill: ChillTracker?, val forecast: WeatherForecast?)

    private val plotData: Flow<PlotData> = focused.flatMapLatest { plot ->
        if (plot == null) {
            flowOf(PlotData(null, null, null))
        } else {
            combine(observeChill(plot.id.value), observeForecast(plot.id.value)) { chill, forecast -> PlotData(plot, chill, forecast) }
        }
    }

    val uiState: StateFlow<HomePhaseUiState> = combine(
        plotData,
        observeCampaignPlans(today().year),
        harvest,
    ) { data, plans, harvest ->
        HomePhaseRules.state(
            today = today(),
            focused = data.plot,
            plan = plans.firstOrNull { it.plotId == data.plot?.id?.value },
            chill = data.chill,
            forecast = data.forecast,
            harvest = harvest,
        )
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

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
