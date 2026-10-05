package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Retrofit client for the backend `/api/v1/agroclimatic-incidents` endpoints. */
interface IncidentService {

    /** Lists summarized agroclimatic incidents with optional plot and status filters. */
    @GET("agroclimatic-incidents")
    suspend fun listIncidents(
        @Query("plotId") plotId: String? = null,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null,
    ): Response<AgroclimaticIncidentsSummaryDto>

    /** Lists summarized agroclimatic incidents scoped to a specific plot. */
    @GET("plots/{plotId}/agroclimatic-incidents")
    suspend fun listPlotIncidents(
        @Path("plotId") plotId: String,
        @Query("status") status: String? = null,
        @Query("severity") severity: String? = null,
    ): Response<AgroclimaticIncidentsSummaryDto>

    /** Retrieves deep incident details including mitigation steps and weekly progression trend. */
    @GET("agroclimatic-incidents/{incidentId}")
    suspend fun getIncidentDetail(
        @Path("incidentId") incidentId: String,
    ): Response<AgroclimaticIncidentDetailDto>

    /** Postpones an incident to snooze notifications for a given number of hours. */
    @POST("agroclimatic-incidents/{incidentId}/postponements")
    suspend fun postponeIncident(
        @Path("incidentId") incidentId: String,
        @Body request: PostponeIncidentRequestDto,
    ): Response<MessageResponseDto>

    /** Marks a mitigation checklist step as completed. */
    @PUT("agroclimatic-incidents/{incidentId}/mitigation-steps/{stepId}")
    suspend fun completeMitigationStep(
        @Path("incidentId") incidentId: String,
        @Path("stepId") stepId: String,
    ): Response<MessageResponseDto>
}
