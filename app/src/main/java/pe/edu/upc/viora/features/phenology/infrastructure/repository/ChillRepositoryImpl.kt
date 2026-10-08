package pe.edu.upc.viora.features.phenology.infrastructure.repository

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
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerDao
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toChillEntity
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.phenology.infrastructure.remote.PhenologyService

class ChillRepositoryImpl @Inject constructor(
    private val service: PhenologyService,
    private val chillTrackerDao: ChillTrackerDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val apiCaller: ApiCaller,
    private val clock: Clock,
) : ChillRepository {

    override fun observeChillTracker(plotId: String): Flow<ChillTracker?> =
        chillTrackerDao.observeByPlot(plotId).map { it?.toDomain() }

    override fun observeLastSync(plotId: String): Flow<Instant?> =
        cacheMetadataDao.observeFetchedAt(cacheKey(plotId)).map { epochMs -> epochMs?.let(Instant::ofEpochMilli) }

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        val remoteMetrics = apiCaller.call { service.getMetrics(plotId, CHILLING_QUERY) }
        val metric = when (remoteMetrics) {
            is AppResult.Success -> remoteMetrics.value.firstOrNull { it.metricName == CHILLING_METRIC }
            is AppResult.Failure -> if (remoteMetrics.error is AppError.NotFound) null else return remoteMetrics
        }

        // No metric means the backend has no chill for this plot: the cache is cleared so no stale winter shows.
        // A metric that cannot be read keeps the cache and reports the failure.
        val entity = metric?.let {
            it.toChillEntity(plotId, clock.millis())
                ?: return AppResult.Failure(AppError.Unknown(IllegalStateException("Malformed chill metric")))
        }

        return guarded {
            if (entity != null) {
                chillTrackerDao.upsert(entity)
                cacheMetadataDao.upsert(CacheMetadataEntity(cacheKey(plotId), clock.millis()))
            } else {
                chillTrackerDao.deleteByPlot(plotId)
            }
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

    private fun cacheKey(plotId: String): String = "chill:$plotId"

    private companion object {
        const val CHILLING_QUERY = "CHILLING"
        const val CHILLING_METRIC = "EREZ_CHILLING_PORTIONS"
    }
}
