package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository

class GetThinningEventsUseCase @Inject constructor(
    private val repository: ThinningRepository,
) {
    suspend operator fun invoke(
        campaignYear: Int? = null,
        plotId: String? = null,
    ): AppResult<List<ThinningEvent>> =
        repository.getThinningEvents(campaignYear, plotId)
}
