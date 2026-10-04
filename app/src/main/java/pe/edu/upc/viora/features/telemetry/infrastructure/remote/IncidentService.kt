package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface IncidentService {

    @GET("agroclimatic-incidents")
    suspend fun listIncidents(
        @Query("plotId") plotId: String? = null,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null,
    ): Response<AgroclimaticIncidentsSummaryDto>

    @GET("plots/{plotId}/agroclimatic-incidents")
    suspend fun listPlotIncidents(
        @Path("plotId") plotId: String,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null,
    ): Response<AgroclimaticIncidentsSummaryDto>

    @GET("agroclimatic-incidents/{incidentId}")
    suspend fun getIncidentDetail(
        @Path("incidentId") incidentId: String,
    ): Response<AgroclimaticIncidentDetailDto>

    @POST("agroclimatic-incidents/{incidentId}/postponements")
    suspend fun postponeIncident(
        @Path("incidentId") incidentId: String,
        @Body request: PostponeIncidentRequestDto,
    ): Response<MessageResponseDto>

    @PUT("agroclimatic-incidents/{incidentId}/mitigation-steps/{stepId}")
    suspend fun completeMitigationStep(
        @Path("incidentId") incidentId: String,
        @Path("stepId") stepId: String,
    ): Response<MessageResponseDto>
}
