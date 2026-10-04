package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class LinkSensorNodeRequestDto(
    val name: String,
    val type: String,
    val depthCm: Int?,
)
