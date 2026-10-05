package pe.edu.upc.viora.features.telemetry.domain.entity

/**
 * An actionable agronomic task within the mitigation plan of an incident.
 *
 * [instruction] is localized and dynamically formatted with relative dates by the backend.
 * [completed] indicates whether the producer performed and verified the action in field.
 */
data class MitigationStep(
    val id: String,
    val instructionKey: String,
    val instruction: String,
    val completed: Boolean,
    val completedAt: String? = null,
)
