package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ForecastService {

    /**
     * 7-day forecast for the centroid of the plot (US19 / TS20). The backend caches it for 3 hours.
     * 400 when the plot has no georeferenced outline.
     */
    @GET("plots/{plotId}/forecasts")
    suspend fun getForecast(@Path("plotId") plotId: String): Response<ForecastDto>
}
