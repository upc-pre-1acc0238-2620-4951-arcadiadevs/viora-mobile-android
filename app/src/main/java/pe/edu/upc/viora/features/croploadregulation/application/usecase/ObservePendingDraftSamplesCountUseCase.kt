package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository

class ObservePendingDraftSamplesCountUseCase @Inject constructor(
    private val repository: ThinningRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observePendingDraftSamplesCount()
}
