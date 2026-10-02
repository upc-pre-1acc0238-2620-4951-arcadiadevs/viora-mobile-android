package pe.edu.upc.viora.features.plotmanagement.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PlotService {

    /** Active plots of the current producer. */
    @GET("plots")
    suspend fun getPlots(): Response<List<PlotDto>>

    /**
     * Registers a plot. 201 with the created plot; 400 for an invalid polygon or payload;
     * 409 when the producer already has a plot with that name.
     */
    @POST("plots")
    suspend fun createPlot(@Body request: CreatePlotRequestDto): Response<PlotDto>
}
