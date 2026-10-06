package pe.edu.upc.viora.features.telemetry.presentation.state

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.telemetry.domain.entity.ForecastDay
import pe.edu.upc.viora.features.telemetry.domain.entity.TelemetrySeries
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast

/** A plot the producer can switch to with the chips of the climate screen. */
data class ClimatePlot(val id: String, val name: String)

/** What the plot climate screen shows (Figma P90; US19 forecast and US17 sensors). */
sealed interface PlotClimateUiState {

    /** Nothing cached for the plot and the first download is in progress. */
    data object Loading : PlotClimateUiState

    /** Nothing cached and the download failed. */
    data class Error(val error: AppError) : PlotClimateUiState

    /**
     * Cached data of the selected plot, possibly stale ([refreshError] tells the last download
     * failed, so the forecast is the stored one). [series] holds the last 7 days of readings.
     */
    data class Content(
        val plots: List<ClimatePlot>,
        val selectedPlotId: String,
        val plotName: String,
        val today: LocalDate,
        val now: Instant,
        val zone: ZoneId,
        val forecast: WeatherForecast?,
        val series: TelemetrySeries,
        val isRefreshing: Boolean,
        val refreshError: AppError?,
    ) : PlotClimateUiState {

        /** Today's forecast, or null when the cached forecast does not reach today. */
        val todayForecast: ForecastDay? get() = forecast?.dayOf(today)

        /** The forecast on screen is the stored one because the last download failed. */
        val isForecastFromCache: Boolean get() = forecast != null && refreshError != null
    }
}
