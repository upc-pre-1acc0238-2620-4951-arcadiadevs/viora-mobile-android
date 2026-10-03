package pe.edu.upc.viora.features.plotmanagement.infrastructure.repository

import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.database.CacheMetadataDao
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.toDomainOrNull
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.toRequestDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.toUpdateRequestDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotDto
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

    override fun observePlot(id: PlotId): Flow<Plot?> =
        plotDao.observeById(id.value).map { row -> row?.toDomainOrNull() }

    override fun observeArchivedPlots(): Flow<List<Plot>> =
        plotDao.observeArchived().map { rows -> rows.mapNotNull { it.toDomainOrNull() } }

    override fun observeLastRefresh(): Flow<Instant?> =
        cacheMetadataDao.observeFetchedAt(PLOTS_CACHE_KEY).map { epochMs -> epochMs?.let(Instant::ofEpochMilli) }

    override suspend fun refresh(): AppResult<Unit> {
        val remote = apiCaller.call { service.getPlots() }
        if (remote is AppResult.Failure) return remote
        val entities = (remote as AppResult.Success).value.map { it.toEntity() }
        return try {
            plotDao.upsertAll(entities)
            plotDao.deleteActiveExcept(entities.map { it.id })
            cacheMetadataDao.upsert(CacheMetadataEntity(PLOTS_CACHE_KEY, clock.millis()))
            AppResult.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unknown(throwable))
        }
    }

    override suspend fun refreshArchived(): AppResult<Unit> {
        val remote = apiCaller.call { service.getArchivedPlots() }
        if (remote is AppResult.Failure) return remote
        val entities = (remote as AppResult.Success).value.map { it.toEntity() }
        return guarded {
            plotDao.upsertAll(entities)
            plotDao.deleteArchivedExcept(entities.map { it.id })
        }
    }

    override suspend fun register(newPlot: NewPlot): AppResult<Plot> =
        apiCaller.call { service.createPlot(newPlot.toRequestDto()) }.cached()

    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> {
        val current = plotDao.observeById(id.value).first()?.toDomainOrNull()
            ?: return AppResult.Failure(AppError.NotFound())
        val result = apiCaller.call { service.updatePlot(id.value, current.revision, current.toUpdateRequestDto(changes)) }
        if (result is AppResult.Failure && result.error is AppError.PreconditionFailed) refresh()
        return result.cached()
    }

    override suspend fun archive(id: PlotId): AppResult<Unit> {
        val result = apiCaller.callUnit { service.archivePlot(id.value) }
        if (result is AppResult.Failure) return result
        return guarded { plotDao.markArchived(id.value) }
    }

    override suspend fun restore(id: PlotId): AppResult<Plot> = apiCaller.call { service.restorePlot(id.value) }.cached()

    /** Stores a plot the server returned and gives it back as a domain plot. */
    private suspend fun AppResult<PlotDto>.cached(): AppResult<Plot> {
        if (this is AppResult.Failure) return this
        val entity = (this as AppResult.Success).value.toEntity()
        val plot = entity.toDomainOrNull()
            ?: return AppResult.Failure(AppError.Unknown(IllegalStateException("Server returned an unreadable plot")))
        return guarded { plotDao.upsertAll(listOf(entity)) }.let { stored ->
            if (stored is AppResult.Failure) stored else AppResult.Success(plot)
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

    private companion object {
        const val PLOTS_CACHE_KEY = "plots"
    }
}
