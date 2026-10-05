package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository

/** Removes a virtual sensor from the active inventory while keeping historical telemetry (US16). */
class UnlinkSensorNodeUseCase @Inject constructor(
    private val repository: SensorRepository,
) {
    suspend operator fun invoke(plotId: String, nodeId: String): AppResult<Unit> =
        repository.unlinkNode(plotId, nodeId)
}
