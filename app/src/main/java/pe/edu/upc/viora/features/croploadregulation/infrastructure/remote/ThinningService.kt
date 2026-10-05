package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ThinningService {

    /**
     * Registers a batch of tree samples for a plot (P52 / P53).
     * Returns 201 Created with the updated statistical summary.
     */
    @POST("plots/{plotId}/samplings")
    suspend fun submitSamplingBatch(
        @Path("plotId") plotId: String,
        @Body request: SamplingBatchRequestDto,
    ): Response<SamplingSummaryResponseDto>

    /**
     * Gets the current accumulated sampling summary for a plot (P54).
     */
    @GET("plots/{plotId}/samplings/summary")
    suspend fun getSamplingSummary(
        @Path("plotId") plotId: String,
        @Query("campaignYear") campaignYear: Int? = null,
    ): Response<SamplingSummaryResponseDto>

    /**
     * Gets the chronological timeline of thinning and sampling milestones (P50 Bitácora).
     */
    @GET("thinning-events")
    suspend fun getThinningEvents(
        @Query("campaignYear") campaignYear: Int? = null,
        @Query("plotId") plotId: String? = null,
    ): Response<ThinningEventsResponseDto>

    /**
     * Gets all producer plots with their sampling status and progress for [campaignYear] (P51).
     */
    @GET("samplings")
    suspend fun getPlotSamplingStates(
        @Query("campaignYear") campaignYear: Int? = null,
    ): Response<List<PlotSamplingStateResponseDto>>
}
