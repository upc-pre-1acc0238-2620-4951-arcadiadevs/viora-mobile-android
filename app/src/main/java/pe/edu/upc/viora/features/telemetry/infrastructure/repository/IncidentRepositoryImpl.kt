package pe.edu.upc.viora.features.telemetry.infrastructure.repository

import java.time.Clock
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.database.CacheMetadataDao
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.infrastructure.local.IncidentDao
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.IncidentService
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.PostponeIncidentRequestDto

class IncidentRepositoryImpl @Inject constructor(
    private val service: IncidentService,
    private val incidentDao: IncidentDao,
    private val apiCaller: ApiCaller,
    private val cacheMetadataDao: CacheMetadataDao,
    private val clock: Clock,
) : IncidentRepository {

    override fun observeIncidents(plotId: String?): Flow<List<AgroclimaticIncident>> {
        val flow = if (plotId != null) {
            incidentDao.observeByPlot(plotId)
        } else {
            incidentDao.observeAll()
        }
        return flow.map { rows -> rows.map { it.toDomain() } }
    }

    override suspend fun refresh(
        plotId: String?,
        status: String?,
        severity: String?,
    ): AppResult<AlertsSummary> {
        val remote = apiCaller.call {
            if (plotId != null) {
                service.listPlotIncidents(plotId = plotId, status = status, severity = severity)
            } else {
                service.listIncidents(plotId = plotId, status = status, severity = severity)
            }
        }
        if (remote is AppResult.Failure) return remote

        val summaryDto = (remote as AppResult.Success).value
        val entities = summaryDto.incidents.map { it.toEntity() }

        val cacheResult = guarded {
            if (plotId != null) {
                incidentDao.upsertAll(entities)
                incidentDao.deleteExceptForPlot(plotId, entities.map { it.id })
                if (status == null && severity == null) stampRefresh(plotId)
            } else if (status == null && severity == null) {
                incidentDao.upsertAll(entities)
                incidentDao.deleteExcept(entities.map { it.id })
                stampRefresh(null)
            } else {
                incidentDao.upsertAll(entities)
            }
        }

        return when (cacheResult) {
            is AppResult.Failure -> cacheResult
            is AppResult.Success -> AppResult.Success(summaryDto.summary.toDomain())
        }
    }

    /** A plot counts as downloaded by its own refresh or by the refresh of every plot. */
    override fun observeLastRefresh(plotId: String?): Flow<Long?> {
        val all = cacheMetadataDao.observeFetchedAt(cacheKey(null))
        if (plotId == null) return all
        return combine(all, cacheMetadataDao.observeFetchedAt(cacheKey(plotId))) { everyPlot, onePlot ->
            listOfNotNull(everyPlot, onePlot).maxOrNull()
        }
    }

    private suspend fun stampRefresh(plotId: String?) {
        cacheMetadataDao.upsert(CacheMetadataEntity(cacheKey(plotId), clock.millis()))
    }

    override suspend fun getIncidentDetail(incidentId: String): AppResult<IncidentDetail> {
        val remote = apiCaller.call { service.getIncidentDetail(incidentId) }
        return when (remote) {
            is AppResult.Failure -> remote
            is AppResult.Success -> AppResult.Success(remote.value.toDomain())
        }
    }

    override suspend fun postponeIncident(incidentId: String, durationHours: Int): AppResult<Unit> {
        val remote = apiCaller.call {
            service.postponeIncident(
                incidentId = incidentId,
                request = PostponeIncidentRequestDto(durationHours = durationHours),
            )
        }
        return when (remote) {
            is AppResult.Failure -> remote
            is AppResult.Success -> guarded {
                incidentDao.updatePostponed(
                    incidentId = incidentId,
                    status = IncidentStatus.SNOOZED.name,
                    snoozedUntil = null,
                )
            }
        }
    }

    override suspend fun completeMitigationStep(incidentId: String, stepId: String): AppResult<Unit> {
        val remote = apiCaller.call {
            service.completeMitigationStep(incidentId = incidentId, stepId = stepId)
        }
        return when (remote) {
            is AppResult.Failure -> remote
            is AppResult.Success -> AppResult.Success(Unit)
        }
    }

    override suspend fun getSummary(): AppResult<AlertsSummary> {
        val remote = apiCaller.call { service.listIncidents() }
        return when (remote) {
            is AppResult.Failure -> remote
            is AppResult.Success -> AppResult.Success(remote.value.summary.toDomain())
        }
    }

    private fun cacheKey(plotId: String?) = if (plotId == null) "incidents" else "incidents:$plotId"

    private suspend fun guarded(block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unknown(throwable))
    }
}
