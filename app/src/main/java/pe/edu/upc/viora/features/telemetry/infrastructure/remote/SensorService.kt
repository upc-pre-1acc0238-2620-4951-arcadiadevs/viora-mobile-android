package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SensorService {

    /** Returns all IoT sensor devices / nodes registered in a plot (US14). */
    @GET("plots/{plotId}/iot-devices")
    suspend fun getNodes(@Path("plotId") plotId: String): Response<List<SensorNodeDto>>

    /**
     * Links a new virtual sensor node to a plot (US13).
     * 201 with created node; 400 validation; 409 duplicated name in the plot.
     */
    @POST("plots/{plotId}/iot-devices")
    suspend fun linkNode(
        @Path("plotId") plotId: String,
        @Body request: LinkSensorNodeRequestDto,
    ): Response<SensorNodeDto>
}
