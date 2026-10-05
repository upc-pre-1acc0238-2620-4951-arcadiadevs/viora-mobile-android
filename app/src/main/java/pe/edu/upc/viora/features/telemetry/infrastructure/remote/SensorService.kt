package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
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

    /** Updates node label and soil depth through the calibration endpoint (US15 / TS42). */
    @PUT("plots/{plotId}/iot-devices/{deviceId}")
    suspend fun updateNode(
        @Path("plotId") plotId: String,
        @Path("deviceId") deviceId: String,
        @Body request: CalibrateSensorNodeRequestDto,
    ): Response<SensorNodeDto>

    /** Logically unlinks the node while preserving its historical telemetry (US16 / TS18). */
    @DELETE("plots/{plotId}/iot-devices/{deviceId}")
    suspend fun unlinkNode(
        @Path("plotId") plotId: String,
        @Path("deviceId") deviceId: String,
    ): Response<Unit>
}
