package pe.edu.upc.viora.features.phenology.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PhenologyService {

    /** Returns every harvest record registered for a plot (US20). */
    @GET("plots/{plotId}/harvest-records")
    suspend fun getRecords(@Path("plotId") plotId: String): Response<List<HarvestRecordDto>>

    /** Registers a past campaign. 201 with the record; 400 validation; 409 campaign already registered. */
    @POST("plots/{plotId}/harvest-records")
    suspend fun recordYield(
        @Path("plotId") plotId: String,
        @Body request: RecordHarvestYieldRequestDto,
    ): Response<HarvestRecordDto>

    @PUT("plots/{plotId}/harvest-records/{recordId}")
    suspend fun rectifyYield(
        @Path("plotId") plotId: String,
        @Path("recordId") recordId: String,
        @Body request: RectifyHarvestYieldRequestDto,
    ): Response<HarvestRecordDto>

    /** 200 with a message body (ignored) on success; 404 unknown record; 412 stale version. */
    @DELETE("plots/{plotId}/harvest-records/{recordId}")
    suspend fun removeRecord(
        @Path("plotId") plotId: String,
        @Path("recordId") recordId: String,
    ): Response<Unit>

    /** Plot metrics by name. 404 means the tracker is not initialized (no records yet). */
    @GET("plots/{plotId}/metrics")
    suspend fun getMetrics(
        @Path("plotId") plotId: String,
        @Query("metricName") metricName: String,
    ): Response<List<MetricDto>>
}
