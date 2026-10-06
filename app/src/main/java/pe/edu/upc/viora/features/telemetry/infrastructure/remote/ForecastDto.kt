package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * DTO for `GET /api/v1/plots/{plotId}/forecasts` (TS20, `ForecastResource`).
 *
 * The field names follow the domain model of the report (tables 72-73); check them against the
 * backend Swagger and adjust here if they differ. Dates are ISO-8601 strings.
 */
@Serializable
data class ForecastDto(
    val syncedAt: String,
    val days: List<ForecastDayDto>,
)

@Serializable
data class ForecastDayDto(
    val forecastDate: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val precipitationProbability: Double,
    val windSpeedKmh: Double,
)
