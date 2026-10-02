package pe.edu.upc.viora.features.plotmanagement.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET

interface PlotService {

    /** Active plots of the current producer. */
    @GET("plots")
    suspend fun getPlots(): Response<List<PlotDto>>
}
