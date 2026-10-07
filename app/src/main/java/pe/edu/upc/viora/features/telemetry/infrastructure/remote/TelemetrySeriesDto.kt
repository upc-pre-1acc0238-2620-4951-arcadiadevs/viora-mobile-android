package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * One item of `GET /api/v1/plots/{plotId}/telemetries` (US17 / TS19, backend `TelemetryResource`):
 * the endpoint answers a plain array of readings. The node has a single soil probe, which the
 * design places at 30 cm (Figma P90 "sonda a 30 cm").
 */
@Serializable
data class HourlyReadingDto(
    val recordedAt: String,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val soilMoisture: Double? = null,
    val id: String? = null,
    val plotId: String? = null,
    val solarRadiation: Double? = null,
    val stemWaterPotential: Double? = null,
)
