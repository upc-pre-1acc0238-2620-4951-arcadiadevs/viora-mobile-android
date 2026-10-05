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
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.repository.HarvestRecordRepository
import pe.edu.upc.viora.features.phenology.infrastructure.local.BearingIndexDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.BearingIndexEntity
import pe.edu.upc.viora.features.phenology.infrastructure.local.HarvestRecordDao
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.recordRequestDto
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.rectifyRequestDto
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.phenology.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.phenology.infrastructure.remote.HarvestRecordDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.PhenologyService

class HarvestRecordRepositoryImpl @Inject constructor(
    private val service: PhenologyService,
    private val recordDao: HarvestRecordDao,
    private val indexDao: BearingIndexDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val apiCaller: ApiCaller,
    private val clock: Clock,
) : HarvestRecordRepository {

    override fun observeRecords(plotId: String): Flow<List<HarvestRecord>> =
        recordDao.observeByPlot(plotId).map { rows -> rows.map { it.toDomain() } }

    override fun observeBearingIndex(plotId: String): Flow<BearingIndex?> =
        indexDao.observeByPlot(plotId).map { row -> row?.toDomain() }

    override fun observeLastRefresh(plotId: String): Flow<Instant?> =
        cacheMetadataDao.observeFetchedAt(cacheKey(plotId)).map { epochMs -> epochMs?.let(Instant::ofEpochMilli) }

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        val remoteRecords = apiCaller.call { service.getRecords(plotId) }
        if (remoteRecords is AppResult.Failure) return remoteRecords
        val remoteMetrics = apiCaller.call { service.getMetrics(plotId, BBI_METRIC) }
        // 404: the server has no tracker for the plot yet (no records), so there is no index.
        val index: BearingIndexEntity? = when {
            remoteMetrics is AppResult.Success ->
                remoteMetrics.value.firstOrNull { it.metricName == BBI_METRIC }?.toEntity(plotId)
            (remoteMetrics as AppResult.Failure).error is AppError.NotFound -> null
            else -> return remoteMetrics
        }
        val entities = (remoteRecords as AppResult.Success).value.map { it.toEntity() }
        return guarded {
            recordDao.replaceForPlot(plotId, entities)
            if (index != null) indexDao.upsert(index) else indexDao.deleteByPlot(plotId)
            cacheMetadataDao.upsert(CacheMetadataEntity(cacheKey(plotId), clock.millis()))
        }
    }

    override suspend fun record(plotId: String, campaignYear: Int, totalYieldKg: Double): AppResult<HarvestRecord> =
        apiCaller.call { service.recordYield(plotId, recordRequestDto(campaignYear, totalYieldKg)) }
            .cachedAndRefreshed(plotId)

    override suspend fun rectify(plotId: String, recordId: String, totalYieldKg: Double): AppResult<HarvestRecord> =
        apiCaller.call { service.rectifyYield(plotId, recordId, rectifyRequestDto(totalYieldKg)) }
            .cachedAndRefreshed(plotId)

    override suspend fun remove(plotId: String, recordId: String): AppResult<Unit> {
        val result = apiCaller.callUnit { service.removeRecord(plotId, recordId) }
        if (result is AppResult.Failure) return result
        val dropped = guarded {
            recordDao.deleteById(recordId)
            indexDao.deleteByPlot(plotId)
        }
        if (dropped is AppResult.Failure) return dropped
        refresh(plotId)
        return result
    }

    /**
     * Stores the mutated record, then refreshes so the list and the index are current. The
     * mutation already succeeded on the server, so a failed refresh does not fail it. The cached
     * index is dropped first: it no longer matches the records, and without it the screen falls
     * back to the local formula until a refresh brings the server's value.
     */
    private suspend fun AppResult<HarvestRecordDto>.cachedAndRefreshed(plotId: String): AppResult<HarvestRecord> {
        if (this is AppResult.Failure) return this
        val entity = (this as AppResult.Success).value.toEntity()
        val stored = guarded {
            recordDao.upsertAll(listOf(entity))
            indexDao.deleteByPlot(plotId)
        }
        if (stored is AppResult.Failure) return stored
        refresh(plotId)
        return AppResult.Success(entity.toDomain())
    }

    private suspend fun guarded(block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unknown(throwable))
    }

    private fun cacheKey(plotId: String) = "harvest:$plotId"

    private companion object {
        const val BBI_METRIC = "BIENNIAL_BEARING_INDEX"
    }
}
