package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

/** When the incidents of a plot (or of every plot, with null) were last downloaded; null if never. */
class ObserveIncidentsLastRefreshUseCase @Inject constructor(
    private val repository: IncidentRepository,
) {
    operator fun invoke(plotId: String? = null): Flow<Long?> = repository.observeLastRefresh(plotId)
}
