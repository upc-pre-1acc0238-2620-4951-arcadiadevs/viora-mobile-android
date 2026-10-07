package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveTelemetryLastRefreshUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveTelemetrySeriesUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshTelemetrySeriesUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.TelemetrySeries
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange
import pe.edu.upc.viora.features.telemetry.presentation.state.TelemetryDetailUiState

/**
 * View model of the metric detail screen (Figma P91): the curve of one metric over 24 hours,
 * 7 or 30 days with its last value (US17, scenario 1). Opens on 7 days, as in the design.
 */
@HiltViewModel
class TelemetryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlot: ObservePlotUseCase,
    private val observeSeries: ObserveTelemetrySeriesUseCase,
    observeLastRefresh: ObserveTelemetryLastRefreshUseCase,
    private val refreshSeries: RefreshTelemetrySeriesUseCase,
    private val clock: Clock,
) : ViewModel() {

    val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "TelemetryDetailRoute needs a plotId" }
    private val routePlotName: String = savedStateHandle.get<String>("plotName").orEmpty()

    private val metric = MutableStateFlow(
        savedStateHandle.get<String>("metric")
            ?.let { name -> TelemetryMetric.entries.firstOrNull { it.name == name } }
            ?: TelemetryMetric.SOIL_MOISTURE,
    )
    private val range = MutableStateFlow(TelemetryRange.LAST_7_DAYS)

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private data class Window(
        val range: TelemetryRange,
        val series: TelemetrySeries,
        val lastRefresh: Instant?,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private var refreshJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val window = range.flatMapLatest { selected ->
        combine(observeSeries(plotId, selected), observeLastRefresh(plotId)) { series, lastRefresh ->
            Window(selected, series, lastRefresh)
        }
    }

    val uiState: StateFlow<TelemetryDetailUiState> = combine(
        window,
        metric,
        observePlot(PlotId(plotId)),
        refreshState,
    ) { window, metric, plot, refresh ->
        val hasData = window.series.points(metric).isNotEmpty()
        when {
            hasData || (refresh.hasFinishedOnce && refresh.error == null) -> TelemetryDetailUiState.Content(
                metric = metric,
                range = window.range,
                plotName = routePlotName.ifBlank { plot?.name.orEmpty() },
                series = window.series,
                now = clock.instant(),
                zone = clock.zone,
                lastRefresh = window.lastRefresh,
                isRefreshing = refresh.isRefreshing,
                refreshError = refresh.error,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> TelemetryDetailUiState.Loading(metric, window.range)
            else -> TelemetryDetailUiState.Error(metric, window.range, checkNotNull(refresh.error))
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        TelemetryDetailUiState.Loading(metric.value, range.value),
    )

    init {
        refresh()
    }

    fun selectMetric(selected: TelemetryMetric) {
        metric.value = selected
    }

    fun selectRange(selected: TelemetryRange) {
        if (selected == range.value) return
        range.value = selected
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        val selected = range.value
        refreshState.update { it.copy(isRefreshing = true) }
        refreshJob = viewModelScope.launch {
            val result = refreshSeries(plotId, selected)
            refreshState.value = RefreshState(
                isRefreshing = false,
                hasFinishedOnce = true,
                error = (result as? AppResult.Failure)?.error,
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
