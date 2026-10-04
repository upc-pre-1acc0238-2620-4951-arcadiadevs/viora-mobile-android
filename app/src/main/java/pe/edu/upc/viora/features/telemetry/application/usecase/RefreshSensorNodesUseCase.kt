package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository

class RefreshSensorNodesUseCase @Inject constructor(
    private val repository: SensorRepository,
) {
    suspend operator fun invoke(plotId: String): AppResult<Unit> = repository.refresh(plotId)
}
