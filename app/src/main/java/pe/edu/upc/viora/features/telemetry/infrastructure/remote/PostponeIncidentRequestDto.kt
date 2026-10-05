package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/** Body of `POST /api/v1/agroclimatic-incidents/{id}/postponements` (`PostponeIncidentResource` of the backend). */
@Serializable
data class PostponeIncidentRequestDto(
    val durationHours: Int,
)
