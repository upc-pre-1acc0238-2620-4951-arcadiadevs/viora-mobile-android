package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository

/** Links a new virtual sensor node to a plot (US13). */
class LinkSensorNodeUseCase @Inject constructor(
    private val repository: SensorRepository,
) {
    suspend operator fun invoke(newNode: NewSensorNode): AppResult<SensorNode> =
        repository.linkNode(newNode)
}
