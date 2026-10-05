package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

/** Synchronizes incidents from the backend into the local cache and returns updated counts. */
class RefreshIncidentsUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    suspend operator fun invoke(
        plotId: String? = null,
        status: String? = null,
        severity: String? = null,
    ): AppResult<AlertsSummary> =
        repository.refresh(plotId = plotId, status = status, severity = severity)
}
