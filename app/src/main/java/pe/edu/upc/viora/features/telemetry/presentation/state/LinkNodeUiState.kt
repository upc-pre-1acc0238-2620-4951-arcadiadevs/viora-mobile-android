package pe.edu.upc.viora.features.telemetry.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType

/**
 * State of the "Vincular un nodo" bottom sheet (US13).
 *
 * [depthCm] is only applicable when [type] is [SensorType.SONDA_SUELO].
 */
data class LinkNodeUiState(
    val name: String = "",
    val type: SensorType = SensorType.SONDA_SUELO,
    val depthCm: Int = 30,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val nameError: Boolean = false,
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && !isLoading
}
