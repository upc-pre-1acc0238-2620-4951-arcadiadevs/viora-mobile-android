package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

/** Postpones (snoozes) alerts for an active incident by a specified number of hours. */
class PostponeIncidentUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    suspend operator fun invoke(incidentId: String, durationHours: Int): AppResult<Unit> =
        repository.postponeIncident(incidentId = incidentId, durationHours = durationHours)
}
