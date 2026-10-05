package pe.edu.upc.viora.features.croploadregulation.infrastructure.repository

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.map
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper.toRequestDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.SamplingBatchRequestDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ThinningService

class ThinningRepositoryImpl @Inject constructor(
    private val service: ThinningService,
    private val apiCaller: ApiCaller,
) : ThinningRepository {

    override suspend fun getPlotSamplingOverview(campaignYear: Int?): AppResult<List<PlotSamplingOverview>> =
        apiCaller.call { service.getPlotSamplingStates(campaignYear) }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getSamplingSummary(plotId: String, campaignYear: Int?): AppResult<SamplingSummary> =
        apiCaller.call { service.getSamplingSummary(plotId, campaignYear) }
            .map { it.toDomain() }

    override suspend fun submitSamplingBatch(
        plotId: String,
        campaignYear: Int,
        batchId: String,
        samples: List<TreeSample>,
    ): AppResult<SamplingSummary> {
        val payload = SamplingBatchRequestDto(
            campaignYear = campaignYear,
            samplingBatchId = batchId,
            samples = samples.map { it.toRequestDto() },
        )
        return apiCaller.call { service.submitSamplingBatch(plotId, payload) }
            .map { it.toDomain() }
    }

    override suspend fun getThinningEvents(
        campaignYear: Int?,
        plotId: String?,
    ): AppResult<List<ThinningEvent>> =
        apiCaller.call { service.getThinningEvents(campaignYear, plotId) }
            .map { envelope -> envelope.events.map { it.toDomain() } }
}
