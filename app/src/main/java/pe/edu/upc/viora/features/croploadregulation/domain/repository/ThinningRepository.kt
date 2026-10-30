package pe.edu.upc.viora.features.croploadregulation.domain.repository

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.DraftSample
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
     * Observes the cached Bitácora timeline, newest first; null until the feed was downloaded once.
     */
    fun observeThinningEvents(): kotlinx.coroutines.flow.Flow<List<ThinningEvent>?>

    /**
     * Observes the current in-progress sampling overview if one exists.
     */
    fun observeActiveSampling(): kotlinx.coroutines.flow.Flow<PlotSamplingOverview?>

    /**
     * Observes the total count of draft samples saved locally on the phone.
     */
    fun observePendingDraftSamplesCount(): kotlinx.coroutines.flow.Flow<Int>

    // ---- Tree samples kept on the phone until the server takes them ----------------------------

    /** Keeps [sample] of [plotId] for [campaignYear], to be sent with [batchId]; a sample of the same tree is replaced. */
    suspend fun saveDraftSample(plotId: String, plotName: String, campaignYear: Int, sample: TreeSample, batchId: String): DraftSample

    /** The samples kept for [plotId] in [campaignYear], oldest first. */
    fun observeDrafts(plotId: String, campaignYear: Int): kotlinx.coroutines.flow.Flow<List<DraftSample>>

    /** Every sample of any plot the server has not taken yet, oldest first. */
    suspend fun unsyncedDrafts(): List<DraftSample>

    /** Gives a draft saved before batch ids existed the one it will keep from now on. */
    suspend fun assignBatchId(draftId: String, batchId: String)

    /** The server took the draft: it is never sent again. */
    suspend fun markSynced(draftId: String)

    /** The server refused the draft for good (409): it leaves the phone. */
    suspend fun deleteDraft(draftId: String)

    /** The round of [plotId] in [campaignYear] is finished: its kept samples leave the phone. */
    suspend fun clearDrafts(plotId: String, campaignYear: Int)
}
