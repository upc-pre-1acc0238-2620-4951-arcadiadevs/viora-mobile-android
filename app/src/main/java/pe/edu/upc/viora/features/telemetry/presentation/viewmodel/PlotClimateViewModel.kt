package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveForecastUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveTelemetrySeriesUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshForecastUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshTelemetrySeriesUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.TelemetrySeries
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange
import pe.edu.upc.viora.features.telemetry.presentation.state.ClimatePlot
import pe.edu.upc.viora.features.telemetry.presentation.state.PlotClimateUiState

/**
 * View model of the plot climate screen (Figma P90): the 7-day forecast (US19) and the last
 * week of the plot's sensors (US17) for the selected plot. The cache is the source of truth;
 * a download failure keeps showing the stored data and tells the screen it is stale.
 */
@HiltViewModel
class PlotClimateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlots: ObservePlotsUseCase,
    private val observeForecast: ObserveForecastUseCase,
    private val observeSeries: ObserveTelemetrySeriesUseCase,
    private val refreshForecast: RefreshForecastUseCase,
    private val refreshSeries: RefreshTelemetrySeriesUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val routePlotName: String = savedStateHandle.get<String>("plotName").orEmpty()
    private val selectedPlotId = MutableStateFlow(
        checkNotNull(savedStateHandle.get<String>("plotId")) { "PlotClimateRoute needs a plotId" },
    )

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private data class PlotData(
        val plotId: String,
        val forecast: WeatherForecast?,
        val series: TelemetrySeries,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private var refreshJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val plotData = selectedPlotId.flatMapLatest { plotId ->
        combine(
            observeForecast(plotId),
            observeSeries(plotId, TelemetryRange.LAST_7_DAYS),
        ) { forecast, series -> PlotData(plotId, forecast, series) }
    }

    val uiState: StateFlow<PlotClimateUiState> = combine(
        plotData,
        observePlots(),
        refreshState,
    ) { data, plots, refresh ->
        val hasData = data.forecast != null || !data.series.isEmpty
        when {
            hasData || (refresh.hasFinishedOnce && refresh.error == null) -> PlotClimateUiState.Content(
                plots = plots.map { ClimatePlot(id = it.id.value, name = it.name) },
                selectedPlotId = data.plotId,
                plotName = plots.firstOrNull { it.id.value == data.plotId }?.name ?: routePlotName,
                today = clock.instant().atZone(clock.zone).toLocalDate(),
                now = clock.instant(),
                zone = clock.zone,
                forecast = data.forecast,
                series = data.series,
                isRefreshing = refresh.isRefreshing,
                refreshError = refresh.error,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> PlotClimateUiState.Loading
            else -> PlotClimateUiState.Error(checkNotNull(refresh.error))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlotClimateUiState.Loading)

    init {
        refresh()
    }

    /** Switches the screen to another plot (the chips) and downloads its data. */
    fun selectPlot(plotId: String) {
        if (plotId == selectedPlotId.value) return
        selectedPlotId.value = plotId
        refreshState.value = RefreshState()
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        val plotId = selectedPlotId.value
        refreshState.update { it.copy(isRefreshing = true) }
        refreshJob = viewModelScope.launch {
            val forecast = refreshForecast(plotId)
            val series = refreshSeries(plotId, TelemetryRange.LAST_7_DAYS)
            refreshState.value = RefreshState(
                isRefreshing = false,
                hasFinishedOnce = true,
                error = (forecast as? AppResult.Failure)?.error ?: (series as? AppResult.Failure)?.error,
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
