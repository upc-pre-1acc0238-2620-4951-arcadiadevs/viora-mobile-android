package pe.edu.upc.viora.features.telemetry.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode

/** State for US15: configure a virtual soil sensor node. */
data class ConfigureNodeUiState(
    val node: SensorNode? = null,
    val nodeCount: Int = 0,
    val plotName: String = "",
    val name: String = "",
    val depthCm: Int = 30,
    val transmitReadings: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUnlinking: Boolean = false,
    val error: AppError? = null,
    val nameError: Boolean = false,
    val depthError: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && depthCm in setOf(30, 60) && !isSaving && !isUnlinking
}
