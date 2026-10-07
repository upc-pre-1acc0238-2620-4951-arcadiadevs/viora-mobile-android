package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository

class GetSamplingSummaryUseCase @Inject constructor(
    private val repository: ThinningRepository,
) {
    suspend operator fun invoke(plotId: String, campaignYear: Int? = null): AppResult<SamplingSummary> =
        repository.getSamplingSummary(plotId, campaignYear)
}
