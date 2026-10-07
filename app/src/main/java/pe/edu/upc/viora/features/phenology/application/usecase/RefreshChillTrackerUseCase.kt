package pe.edu.upc.viora.features.phenology.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository

class RefreshChillTrackerUseCase @Inject constructor(
    private val repository: ChillRepository,
) {
    suspend operator fun invoke(plotId: String): AppResult<Unit> = repository.refresh(plotId)
}
