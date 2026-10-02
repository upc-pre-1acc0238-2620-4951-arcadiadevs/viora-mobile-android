package pe.edu.upc.viora.features.plotmanagement.infrastructure.repository

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
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.toDomainOrNull
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotService

class PlotRepositoryImpl @Inject constructor(
    private val service: PlotService,
    private val plotDao: PlotDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val apiCaller: ApiCaller,
    private val clock: Clock,
) : PlotRepository {

    override fun observePlots(): Flow<List<Plot>> =
        plotDao.observeActive().map { rows -> rows.mapNotNull { it.toDomainOrNull() } }

    override fun observeLastRefresh(): Flow<Instant?> =
        cacheMetadataDao.observeFetchedAt(PLOTS_CACHE_KEY).map { epochMs -> epochMs?.let(Instant::ofEpochMilli) }

    override suspend fun refresh(): AppResult<Unit> {
        val remote = apiCaller.call { service.getPlots() }
        if (remote is AppResult.Failure) return remote
        val entities = (remote as AppResult.Success).value.map { it.toEntity() }
        return try {
            plotDao.upsertAll(entities)
            plotDao.deleteAllExcept(entities.map { it.id })
            cacheMetadataDao.upsert(CacheMetadataEntity(PLOTS_CACHE_KEY, clock.millis()))
            AppResult.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unknown(throwable))
        }
    }

    private companion object {
        const val PLOTS_CACHE_KEY = "plots"
    }
}
