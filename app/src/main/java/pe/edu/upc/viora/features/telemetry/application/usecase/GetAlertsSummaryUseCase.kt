package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

/** Retrieves the current quantitative alert counts for the notification bell and Home card. */
class GetAlertsSummaryUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    suspend operator fun invoke(): AppResult<AlertsSummary> =
        repository.getSummary()
}
