package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository

/** The cached Bitácora timeline; null until it was downloaded once. */
class ObserveThinningEventsUseCase @Inject constructor(
    private val repository: ThinningRepository,
) {
    operator fun invoke(): Flow<List<ThinningEvent>?> = repository.observeThinningEvents()
}
