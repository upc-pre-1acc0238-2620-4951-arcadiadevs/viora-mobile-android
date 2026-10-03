package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository

/** Archived plots from the local cache, ordered by name. */
class ObserveArchivedPlotsUseCase @Inject constructor(private val repository: PlotRepository) {
    operator fun invoke(): Flow<List<Plot>> = repository.observeArchivedPlots()
}
