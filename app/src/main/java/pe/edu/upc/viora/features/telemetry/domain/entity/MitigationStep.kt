package pe.edu.upc.viora.features.telemetry.domain.entity

data class MitigationStep(
    val id: String,
    val instructionKey: String,
    val instruction: String,
    val completed: Boolean,
    val completedAt: String? = null,
)
