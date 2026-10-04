package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class MitigationStepDto(
    val id: String,
    val instructionKey: String = "",
    val instruction: String,
    val completed: Boolean = false,
    val completedAt: String? = null,
)
