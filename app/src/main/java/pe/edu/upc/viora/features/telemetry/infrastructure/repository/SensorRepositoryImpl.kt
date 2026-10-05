package pe.edu.upc.viora.features.telemetry.infrastructure.repository

import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository
import pe.edu.upc.viora.features.telemetry.infrastructure.local.SensorNodeDao
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toDomainOrNull
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toCalibrationRequestDto
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toRequestDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.SensorNodeDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.SensorService

class SensorRepositoryImpl @Inject constructor(
    private val service: SensorService,
    private val sensorNodeDao: SensorNodeDao,
    private val apiCaller: ApiCaller,
) : SensorRepository {

    override fun observeNodes(plotId: String): Flow<List<SensorNode>> =
        sensorNodeDao.observeByPlot(plotId).map { rows -> rows.mapNotNull { it.toDomainOrNull() } }

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        val remote = apiCaller.call { service.getNodes(plotId) }
        if (remote is AppResult.Failure) return remote
        val entities = (remote as AppResult.Success).value.map { it.toEntity() }
        return guarded {
            sensorNodeDao.upsertAll(entities)
            sensorNodeDao.deleteExcept(plotId, entities.map { it.id })
        }
    }

    override suspend fun linkNode(newNode: NewSensorNode): AppResult<SensorNode> =
        apiCaller.call { service.linkNode(newNode.plotId, newNode.toRequestDto()) }.cached()

    override suspend fun updateNode(node: SensorNode): AppResult<SensorNode> {
        val result = apiCaller.call {
            service.updateNode(node.plotId, node.id, node.toCalibrationRequestDto())
        }
        if (result is AppResult.Failure) return result

        val remoteEntity = (result as AppResult.Success).value.toEntity()
        // The calibration endpoint owns the technical fields; keep the UI transmission
        // state locally because the API contract does not expose it in TS42.
        val entity = remoteEntity.copy(status = node.status.name)
        val updated = entity.toDomainOrNull()
            ?: return AppResult.Failure(AppError.Unknown(IllegalStateException("Server returned an unreadable node")))
        return guarded { sensorNodeDao.upsertAll(listOf(entity)) }.let { stored ->
            if (stored is AppResult.Failure) stored else AppResult.Success(updated)
        }
    }

    override suspend fun unlinkNode(plotId: String, nodeId: String): AppResult<Unit> {
        val result = apiCaller.callUnit { service.unlinkNode(plotId, nodeId) }
        if (result is AppResult.Failure) return result
        return guarded { sensorNodeDao.delete(plotId, nodeId) }
    }

    private suspend fun AppResult<SensorNodeDto>.cached(): AppResult<SensorNode> {
        if (this is AppResult.Failure) return this
        val entity = (this as AppResult.Success).value.toEntity()
        val node = entity.toDomainOrNull()
            ?: return AppResult.Failure(AppError.Unknown(IllegalStateException("Server returned an unreadable node")))
        return guarded { sensorNodeDao.upsertAll(listOf(entity)) }.let { stored ->
            if (stored is AppResult.Failure) stored else AppResult.Success(node)
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
}
