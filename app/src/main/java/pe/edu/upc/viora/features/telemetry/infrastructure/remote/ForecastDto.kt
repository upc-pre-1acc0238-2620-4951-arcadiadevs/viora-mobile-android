package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * DTO for `GET /api/v1/plots/{plotId}/forecasts` (US19 / TS20, backend `WeatherForecastResource`).
 * Each day carries the time the weather service produced it. Dates are ISO-8601 strings.
 */
@Serializable
data class ForecastDto(
    val plotId: String? = null,
    val dailyForecasts: List<ForecastDayDto> = emptyList(),
)

/** Backend `DailyForecastDto`; [precipitationProbability] is a percentage (0–100). */
@Serializable
data class ForecastDayDto(
    val forecastDate: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val precipitationProbability: Double,
    val windSpeedKmh: Double,
    val isFrostRisk: Boolean? = null,
    val syncedAt: String? = null,
)
