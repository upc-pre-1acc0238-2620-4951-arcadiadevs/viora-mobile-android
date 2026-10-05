package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository

class SubmitSamplingBatchUseCase @Inject constructor(
    private val repository: ThinningRepository,
) {
    suspend operator fun invoke(
        plotId: String,
        campaignYear: Int,
        batchId: String,
        samples: List<TreeSample>,
    ): AppResult<SamplingSummary> =
        repository.submitSamplingBatch(plotId, campaignYear, batchId, samples)
}
