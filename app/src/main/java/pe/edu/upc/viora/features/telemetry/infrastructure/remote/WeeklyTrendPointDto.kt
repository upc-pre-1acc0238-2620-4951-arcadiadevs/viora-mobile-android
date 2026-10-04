package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class WeeklyTrendPointDto(
    val timestamp: String,
    val value: Double,
    val threshold: Double,
)
