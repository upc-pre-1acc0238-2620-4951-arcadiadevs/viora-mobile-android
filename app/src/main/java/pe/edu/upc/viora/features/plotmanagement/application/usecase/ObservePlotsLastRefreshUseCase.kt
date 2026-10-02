package pe.edu.upc.viora.features.plotmanagement.application.usecase

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository

/** When the plot list was last synchronised with the server (`null` if never). */
class ObservePlotsLastRefreshUseCase @Inject constructor(private val repository: PlotRepository) {
    operator fun invoke(): Flow<Instant?> = repository.observeLastRefresh()
}
