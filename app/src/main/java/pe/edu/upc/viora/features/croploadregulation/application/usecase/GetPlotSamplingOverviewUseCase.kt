package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository

class GetPlotSamplingOverviewUseCase @Inject constructor(
    private val repository: ThinningRepository,
) {
    suspend operator fun invoke(campaignYear: Int? = null): AppResult<List<PlotSamplingOverview>> =
        repository.getPlotSamplingOverview(campaignYear)
}

