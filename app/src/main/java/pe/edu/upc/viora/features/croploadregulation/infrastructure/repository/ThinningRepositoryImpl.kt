package pe.edu.upc.viora.features.croploadregulation.infrastructure.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map as flowMap
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
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.SamplingBatchRequestDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ThinningService

class ThinningRepositoryImpl @Inject constructor(
    private val service: ThinningService,
    private val apiCaller: ApiCaller,
    private val draftDao: DraftTreeSampleDao,
) : ThinningRepository {

    override suspend fun getPlotSamplingOverview(campaignYear: Int?): AppResult<List<PlotSamplingOverview>> =
        apiCaller.call { service.getPlotSamplingStates(campaignYear) }
            .map { list ->
                val backendList = list.map { it.toDomain() }
                val drafts = draftDao.getAllSamples()
                if (drafts.isEmpty()) {
                    backendList
                } else {
                    val draftsByPlot = drafts.groupBy { it.plotId }
                    backendList.map { overview ->
                        val plotDrafts = draftsByPlot[overview.plotId]
                        if (!plotDrafts.isNullOrEmpty() && overview.samplingStatus != SamplingStatus.COMPLETED) {
                            val draftCount = plotDrafts.size
                            overview.copy(
                                samplingStatus = SamplingStatus.IN_PROGRESS,
                                sampledTreesCount = draftCount,
                                treesNeeded = (5 - draftCount).coerceAtLeast(0),
                            )
                        } else {
                            overview
                        }
                    }
                }
            }

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
            clientBatchId = batchId,
            campaignYear = campaignYear,
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

    override fun observeActiveSampling(): Flow<PlotSamplingOverview?> =
        draftDao.observeAllSamples().flowMap { drafts ->
            if (drafts.isEmpty()) {
                null
            } else {
                val firstPlotDrafts = drafts.groupBy { it.plotId }.values.firstOrNull() ?: return@flowMap null
                val first = firstPlotDrafts.first()
                val draftCount = firstPlotDrafts.size
                val targetTrees = 5
                PlotSamplingOverview(
                    plotId = first.plotId,
                    plotName = first.plotName,
                    variety = "",
                    areaHectares = 0.0,
                    campaignYear = first.campaignYear,
                    samplingStatus = SamplingStatus.IN_PROGRESS,
                    sampledTreesCount = draftCount,
                    treesNeeded = (targetTrees - draftCount).coerceAtLeast(0),
                    isRepresentative = draftCount >= 5,
                )
            }
        }

    override fun observePendingDraftSamplesCount(): Flow<Int> =
        draftDao.observePendingDraftSamplesCount()
}
