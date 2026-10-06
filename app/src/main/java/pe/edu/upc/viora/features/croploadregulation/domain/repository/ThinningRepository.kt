package pe.edu.upc.viora.features.croploadregulation.domain.repository

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingDetailedReport
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample

interface ThinningRepository {

    /**
     * Retrieves the sampling progress and state for all producer plots for [campaignYear].
     */
    suspend fun getPlotSamplingOverview(campaignYear: Int? = null): AppResult<List<PlotSamplingOverview>>

    /**
     * Retrieves the accumulated sampling statistics for [plotId] and [campaignYear].
     */
    suspend fun getSamplingSummary(plotId: String, campaignYear: Int? = null): AppResult<SamplingSummary>

    /**
     * Retrieves the sampling statistics plus individual tree evaluations (P54 Detailed).
     */
    suspend fun getSamplingDetailed(plotId: String, campaignYear: Int? = null): AppResult<SamplingDetailedReport>

    /**
     * Submits a batch of field tree samples.
     */
    suspend fun submitSamplingBatch(
        plotId: String,
        campaignYear: Int,
        batchId: String,
        samples: List<TreeSample>,
    ): AppResult<SamplingSummary>

    /**
     * Retrieves chronological thinning events (milestones & labors) for the Bitácora timeline.
     */
    suspend fun getThinningEvents(
        campaignYear: Int? = null,
        plotId: String? = null,
    ): AppResult<List<ThinningEvent>>

    /**
     * Observes the current in-progress sampling overview if one exists.
     */
    fun observeActiveSampling(): kotlinx.coroutines.flow.Flow<PlotSamplingOverview?>

    /**
     * Observes the total count of draft samples saved locally on the phone.
     */
    fun observePendingDraftSamplesCount(): kotlinx.coroutines.flow.Flow<Int>
}
