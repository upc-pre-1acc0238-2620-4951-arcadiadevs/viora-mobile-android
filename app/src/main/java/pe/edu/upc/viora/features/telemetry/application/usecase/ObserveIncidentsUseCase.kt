package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

/** Observes the stream of cached agroclimatic incidents from the Room database. */
class ObserveIncidentsUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    operator fun invoke(plotId: String? = null): Flow<List<AgroclimaticIncident>> =
        repository.observeIncidents(plotId)
}
