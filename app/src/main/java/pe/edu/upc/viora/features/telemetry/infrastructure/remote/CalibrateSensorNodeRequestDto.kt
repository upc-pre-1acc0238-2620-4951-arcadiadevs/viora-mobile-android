package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class CalibrateSensorNodeRequestDto(
    val name: String,
    val depthCm: Int,
    val calibrationMultiplier: Double = 1.0,
    val calibrationNotes: String = "",
    /** false pauses the node and true resumes it; the backend keeps the state when it is null. */
    val transmitting: Boolean? = null,
)
