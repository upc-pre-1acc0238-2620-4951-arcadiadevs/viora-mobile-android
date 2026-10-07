package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveForecastUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveTelemetrySeriesUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshForecastUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshTelemetrySeriesUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.TelemetrySeries
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.telemetry.domain.valueobject.HeatLevel
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange
import pe.edu.upc.viora.features.telemetry.presentation.state.HomeClimateUiState
import pe.edu.upc.viora.features.telemetry.presentation.state.HomeHotDay
import pe.edu.upc.viora.features.telemetry.presentation.state.HomeSoil
import pe.edu.upc.viora.features.telemetry.presentation.state.HomeWeather

/**
 * The two climate cards of the Home "Hoy en tu campo" (Figma P10): the weather of the plot in
 * focus and its soil moisture. It reads the same caches as the climate screen (P90), so both
 * always agree, and refreshes them whenever the Home changes the plot in focus.
 */
@HiltViewModel
class HomeClimateViewModel @Inject constructor(
    private val observeForecast: ObserveForecastUseCase,
    private val observeSeries: ObserveTelemetrySeriesUseCase,
    private val refreshForecast: RefreshForecastUseCase,
    private val refreshSeries: RefreshTelemetrySeriesUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val plotId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeClimateUiState> = plotId.filterNotNull().flatMapLatest { id ->
        combine(observeForecast(id), observeSeries(id, TelemetryRange.LAST_7_DAYS)) { forecast, series ->
            HomeClimateUiState(weather = weatherOf(forecast, series), soil = soilOf(series))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeClimateUiState())

    /** The Home tells which plot is in focus; its forecast and readings are brought up to date. */
    fun showPlot(id: String) {
        if (plotId.value == id) return
        plotId.value = id
        viewModelScope.launch {
            launch { refreshForecast(id) }
            launch { refreshSeries(id, TelemetryRange.LAST_7_DAYS) }
        }
    }

    private fun weatherOf(forecast: WeatherForecast?, series: TelemetrySeries): HomeWeather? {
        val today = clock.instant().atZone(clock.zone).toLocalDate()
        val todayForecast = forecast?.dayOf(today)
        val reading = series.latest(TelemetryMetric.TEMPERATURE)
            ?.takeIf { Duration.between(it.observedAt, clock.instant()) <= FRESH_READING }
        if (todayForecast == null && reading == null) return null
        val hottest = forecast?.days
            ?.filter { !it.date.isBefore(today) && it.heat != HeatLevel.NORMAL }
            ?.maxByOrNull { it.maxTemperatureCelsius }
        return HomeWeather(
            // A fresh sensor reading is "now"; without one the card shows today's high instead.
            temperatureCelsius = reading?.value ?: checkNotNull(todayForecast).maxTemperatureCelsius,
            isLiveReading = reading != null,
            sky = todayForecast?.sky,
            maxCelsius = todayForecast?.maxTemperatureCelsius,
            minCelsius = todayForecast?.minTemperatureCelsius,
            hotDay = hottest?.let { HomeHotDay(it.date, it.maxTemperatureCelsius, it.heat == HeatLevel.EXTREME) },
        )
    }

    private fun soilOf(series: TelemetrySeries): HomeSoil? {
        val latest = series.latest(TelemetryMetric.SOIL_MOISTURE) ?: return null
        return HomeSoil(percent = latest.value)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L

        /** An older reading is not "now": the card falls back to the forecast of the day. */
        val FRESH_READING: Duration = Duration.ofHours(3)
    }
}
