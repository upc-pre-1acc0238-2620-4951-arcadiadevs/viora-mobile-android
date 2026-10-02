package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository

/** Active plots from the local cache, ordered by name. */
class ObservePlotsUseCase @Inject constructor(private val repository: PlotRepository) {
    operator fun invoke(): Flow<List<Plot>> = repository.observePlots()
}
