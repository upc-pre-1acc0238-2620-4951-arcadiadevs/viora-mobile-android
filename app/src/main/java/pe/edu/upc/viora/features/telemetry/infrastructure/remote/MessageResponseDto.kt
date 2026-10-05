package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/** Generic message payload (`MessageResource` of the backend). */
@Serializable
data class MessageResponseDto(
    val message: String = "",
)
