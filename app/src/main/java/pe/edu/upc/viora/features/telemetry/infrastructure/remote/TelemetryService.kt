package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TelemetryService {

    /**
     * Hourly readings of the plot between [startDate] and [endDate] (ISO-8601 instants) (US17 / TS19).
     * 400 when the start is after the end.
     */
    @GET("plots/{plotId}/telemetries")
    suspend fun getTelemetries(
        @Path("plotId") plotId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
    ): Response<List<HourlyReadingDto>>
}
