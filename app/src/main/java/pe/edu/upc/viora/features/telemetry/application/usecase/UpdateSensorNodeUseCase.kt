package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository

/** Persists the editable configuration of an existing virtual sensor node (US15). */
class UpdateSensorNodeUseCase @Inject constructor(
    private val repository: SensorRepository,
) {
    suspend operator fun invoke(node: SensorNode): AppResult<SensorNode> = repository.updateNode(node)
}
