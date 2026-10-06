package pe.edu.upc.viora.features.telemetry.domain.entity

import java.time.Instant
import java.time.LocalDate

/**
 * The 7-day forecast of a plot and when it was last synchronised with the weather service
 * (US19). [days] are ordered by date.
 */
data class WeatherForecast(
    val plotId: String,
    val days: List<ForecastDay>,
    val syncedAt: Instant,
) {
    /** The forecast of [date], or null when the cached forecast does not include it. */
    fun dayOf(date: LocalDate): ForecastDay? = days.firstOrNull { it.date == date }

    /** Lowest minimum of the week: the left end of the temperature bars. */
    val weekMinCelsius: Double? get() = days.minOfOrNull { it.minTemperatureCelsius }

    /** Highest maximum of the week: the right end of the temperature bars. */
    val weekMaxCelsius: Double? get() = days.maxOfOrNull { it.maxTemperatureCelsius }
}
