package pe.edu.upc.viora.features.telemetry.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast

interface ForecastRepository {
    /** Emits the cached forecast of the plot, or null when none has been downloaded yet. */
    fun observeForecast(plotId: String): Flow<WeatherForecast?>

    /** Downloads the 7-day forecast and replaces the cached one (US19). Keeps the cache on failure. */
    suspend fun refresh(plotId: String): AppResult<Unit>
}
