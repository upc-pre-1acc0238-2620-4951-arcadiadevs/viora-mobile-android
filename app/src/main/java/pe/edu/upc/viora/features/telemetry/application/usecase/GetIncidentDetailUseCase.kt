package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

/** Retrieves detailed metrics, weekly trends, and mitigation steps for a single incident. */
class GetIncidentDetailUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    suspend operator fun invoke(incidentId: String): AppResult<IncidentDetail> =
        repository.getIncidentDetail(incidentId)
}
