package pe.edu.upc.viora.features.climate.infrastructure.repository

import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.database.CacheMetadataDao
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.climate.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.climate.domain.repository.WeatherForecastRepository
import pe.edu.upc.viora.features.climate.infrastructure.local.WeatherForecastDao
import pe.edu.upc.viora.features.climate.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.climate.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.climate.infrastructure.remote.ClimateService

class WeatherForecastRepositoryImpl @Inject constructor(
    private val service: ClimateService,
    private val forecastDao: WeatherForecastDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val apiCaller: ApiCaller,
    private val clock: Clock,
) : WeatherForecastRepository {

    override fun observeForecast(plotId: String): Flow<WeatherForecast?> =
        forecastDao.observeByPlot(plotId).map { entities ->
            if (entities.isEmpty()) null else entities.toDomain(plotId)
        }

    override fun observeLastRefresh(plotId: String): Flow<Instant?> =
        cacheMetadataDao.observeFetchedAt(cacheKey(plotId)).map { epochMs ->
            epochMs?.let(Instant::ofEpochMilli)
        }

    override suspend fun refreshForecast(plotId: String): AppResult<Unit> {
        val remoteResult = apiCaller.call { service.getPlotForecast(plotId) }
        if (remoteResult is AppResult.Failure) return remoteResult

        val response = (remoteResult as AppResult.Success).value
        val entities = response.dailyForecasts.map { it.toEntity(plotId) }

        return guarded {
            forecastDao.replaceForPlot(plotId, entities)
            cacheMetadataDao.upsert(CacheMetadataEntity(cacheKey(plotId), clock.millis()))
        }
    }

    private suspend fun guarded(block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unknown(throwable))
    }

    companion object {
        fun cacheKey(plotId: String): String = "forecasts:$plotId"
    }
}
