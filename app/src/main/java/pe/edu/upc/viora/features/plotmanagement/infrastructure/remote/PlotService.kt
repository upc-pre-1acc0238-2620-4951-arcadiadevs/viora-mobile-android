package pe.edu.upc.viora.features.plotmanagement.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlotService {

    /** Active plots of the current producer. */
    @GET("plots")
    suspend fun getPlots(): Response<List<PlotDto>>

    /** Archived plots of the current producer. */
    @GET("plots?status=REMOVED_SOFT_DELETE")
    suspend fun getArchivedPlots(): Response<List<PlotDto>>

    /**
     * Registers a plot. 201 with the created plot; 400 for an invalid polygon or payload;
     * 409 when the producer already has a plot with that name.
     */
    @POST("plots")
    suspend fun createPlot(@Body request: CreatePlotRequestDto): Response<PlotDto>

    /**
     * Edits a plot, guarded by its [revision] (`If-Match`). 200 with the updated plot; 400 for an
     * invalid payload; 404 when the plot is not active; 409 when the name is taken; 412 when the
     * plot changed on another device since [revision].
     */
    @PUT("plots/{id}")
    suspend fun updatePlot(
        @Path("id") id: String,
        @Header("If-Match") revision: Long,
        @Body request: UpdatePlotRequestDto,
    ): Response<PlotDto>

    /** Archives the plot (soft delete): it leaves the active inventory but keeps its history. */
    @DELETE("plots/{id}")
    suspend fun archivePlot(@Path("id") id: String): Response<Unit>

    /** Brings an archived plot back to the active ones. 404 when unknown, 409 when it is not archived. */
    @POST("plots/{id}/restore")
    suspend fun restorePlot(@Path("id") id: String): Response<PlotDto>
}
