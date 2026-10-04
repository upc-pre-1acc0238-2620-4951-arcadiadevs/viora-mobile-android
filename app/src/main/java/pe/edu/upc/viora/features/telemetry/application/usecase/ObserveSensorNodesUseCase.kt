package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository

class ObserveSensorNodesUseCase @Inject constructor(
    private val repository: SensorRepository,
) {
    operator fun invoke(plotId: String): Flow<List<SensorNode>> = repository.observeNodes(plotId)
}
