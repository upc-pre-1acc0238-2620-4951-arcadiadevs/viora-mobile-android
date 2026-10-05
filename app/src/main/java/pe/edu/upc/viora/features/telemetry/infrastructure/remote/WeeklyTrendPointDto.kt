package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/** `WeeklyTrendPointResource` representation of a daily microclimatic data point in the backend. */
@Serializable
data class WeeklyTrendPointDto(
    val timestamp: String,
    val value: Double,
    val threshold: Double,
)
