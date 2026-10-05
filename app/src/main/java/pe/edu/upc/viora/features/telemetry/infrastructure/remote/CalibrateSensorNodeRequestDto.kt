package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class CalibrateSensorNodeRequestDto(
    val name: String,
    val depthCm: Int,
    val calibrationMultiplier: Double = 1.0,
    val calibrationNotes: String = "",
)
