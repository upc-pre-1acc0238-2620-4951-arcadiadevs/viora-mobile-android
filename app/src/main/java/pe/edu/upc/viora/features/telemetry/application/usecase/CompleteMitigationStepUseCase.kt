package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

class CompleteMitigationStepUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    suspend operator fun invoke(incidentId: String, stepId: String): AppResult<Unit> =
        repository.completeMitigationStep(incidentId = incidentId, stepId = stepId)
}
