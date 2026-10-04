package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

class GetAlertsSummaryUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    suspend operator fun invoke(): AppResult<AlertsSummary> =
        repository.getSummary()
}
