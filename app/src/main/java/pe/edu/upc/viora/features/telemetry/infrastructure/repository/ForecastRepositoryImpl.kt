package pe.edu.upc.viora.features.telemetry.infrastructure.repository

import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.telemetry.domain.repository.ForecastRepository
import pe.edu.upc.viora.features.telemetry.infrastructure.local.ForecastDayDao
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toForecastOrNull
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.ForecastService

class ForecastRepositoryImpl @Inject constructor(
    private val service: ForecastService,
    private val forecastDao: ForecastDayDao,
    private val apiCaller: ApiCaller,
    private val clock: Clock,
) : ForecastRepository {

    override fun observeForecast(plotId: String): Flow<WeatherForecast?> =
        forecastDao.observeByPlot(plotId).map { rows -> rows.toForecastOrNull(plotId) }

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        val remote = apiCaller.call { service.getForecast(plotId) }
        if (remote is AppResult.Failure) return remote
        val dto = (remote as AppResult.Success).value
        // The sync time is the weather service's, so "updated 6:40 a.m." is when the forecast was
        // produced; if the server sends something unreadable the download time stands in for it.
        val syncedAt = dto.dailyForecasts
            .mapNotNull { day -> day.syncedAt?.let { runCatching { Instant.parse(it) }.getOrNull() } }
            .maxOrNull()?.toEpochMilli() ?: clock.millis()
        val entities = dto.dailyForecasts.mapNotNull { it.toEntity(plotId, syncedAt) }
        // An empty answer must not wipe a good cached forecast.
        if (entities.isEmpty()) {
            return AppResult.Failure(AppError.Unknown(IllegalStateException("Server returned an empty forecast")))
        }
        return guarded { forecastDao.replaceForPlot(plotId, entities) }
    }

    private suspend fun guarded(block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unknown(throwable))
    }
}
