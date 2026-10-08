package pe.edu.upc.viora.features.phenology.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository

class ObserveChillTrackerUseCase @Inject constructor(
    private val repository: ChillRepository,
) {
    operator fun invoke(plotId: String): Flow<ChillTracker?> = repository.observeChillTracker(plotId)
}
