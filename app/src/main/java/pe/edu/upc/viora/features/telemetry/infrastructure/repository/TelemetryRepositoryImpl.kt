package pe.edu.upc.viora.features.telemetry.infrastructure.repository

import java.time.Clock
import java.time.Duration
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
import pe.edu.upc.viora.features.telemetry.domain.entity.HourlyReading
import pe.edu.upc.viora.features.telemetry.domain.repository.TelemetryRepository
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange
import pe.edu.upc.viora.features.telemetry.infrastructure.local.TelemetryReadingDao
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.TelemetryService

class TelemetryRepositoryImpl @Inject constructor(
    private val service: TelemetryService,
    private val readingDao: TelemetryReadingDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val apiCaller: ApiCaller,
    private val clock: Clock,
) : TelemetryRepository {

    override fun observeReadings(plotId: String, since: Instant): Flow<List<HourlyReading>> =
        readingDao.observeSince(plotId, since.toEpochMilli()).map { rows -> rows.map { it.toDomain() } }

    override fun observeLastRefresh(plotId: String): Flow<Instant?> =
        cacheMetadataDao.observeFetchedAt(cacheKey(plotId)).map { epochMs -> epochMs?.let(Instant::ofEpochMilli) }

    override suspend fun refresh(plotId: String, range: TelemetryRange): AppResult<Unit> {
        val end = clock.instant()
        val start = end.minus(Duration.ofHours(range.hours))
        val remote = apiCaller.call { service.getTelemetries(plotId, start.toString(), end.toString()) }
        if (remote is AppResult.Failure) return remote
        val entities = (remote as AppResult.Success).value.mapNotNull { it.toEntity(plotId) }
        return guarded {
            readingDao.upsertAll(entities)
            readingDao.deleteBefore(plotId, end.minus(Duration.ofHours(TelemetryRange.LAST_30_DAYS.hours)).toEpochMilli())
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

    private fun cacheKey(plotId: String) = "telemetry:$plotId"
}
