package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class PostponeIncidentRequestDto(
    val durationHours: Int,
)
