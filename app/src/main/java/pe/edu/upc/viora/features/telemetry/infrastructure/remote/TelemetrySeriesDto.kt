package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * DTO for `GET /api/v1/plots/{plotId}/telemetries` (TS19, `TelemetrySeriesResource`).
 *
 * The field names follow the domain model of the report (tables 69-70); check them against the
 * backend Swagger and adjust here if they differ: nothing outside `infrastructure` depends on them.
 */
@Serializable
data class TelemetrySeriesDto(
    val readings: List<HourlyReadingDto>,
)

@Serializable
data class HourlyReadingDto(
    val observedAt: String,
    val airTemperature: Double? = null,
    val relativeHumidity: Double? = null,
    val soilMoisture30cm: Double? = null,
    val soilMoisture60cm: Double? = null,
)
