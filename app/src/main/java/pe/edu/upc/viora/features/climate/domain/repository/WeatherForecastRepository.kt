package pe.edu.upc.viora.features.climate.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.climate.domain.entity.WeatherForecast

/**
 * Access to the 7-day meteorological forecast for an olive plot.
 * Follows an offline-first contract backed by Room with background network synchronization.
 */
interface WeatherForecastRepository {

    /**
     * Observes the cached 7-day forecast for the given [plotId].
     * Emits `null` if no forecast has been persisted yet.
     */
    fun observeForecast(plotId: String): Flow<WeatherForecast?>

    /**
     * Observes the timestamp of the last successful remote synchronization for [plotId].
     */
    fun observeLastRefresh(plotId: String): Flow<Instant?>

    /**
     * Fetches the latest 7-day weather forecast from the backend API for [plotId]
     * and atomically updates the local database and cache metadata.
     */
    suspend fun refreshForecast(plotId: String): AppResult<Unit>
}
