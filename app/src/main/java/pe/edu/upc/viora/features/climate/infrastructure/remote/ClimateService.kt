package pe.edu.upc.viora.features.climate.infrastructure.remote

import pe.edu.upc.viora.features.climate.infrastructure.remote.dto.WeatherForecastResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit contract for Viora's agroclimatic and weather forecast endpoints.
 */
interface ClimateService {

    /**
     * Retrieves the geolocalized 7-day weather forecast for the specified plot.
     */
    @GET("plots/{plotId}/forecasts")
    suspend fun getPlotForecast(
        @Path("plotId") plotId: String,
    ): Response<WeatherForecastResponseDto>
}
