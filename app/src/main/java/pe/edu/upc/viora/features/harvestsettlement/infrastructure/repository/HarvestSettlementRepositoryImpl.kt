package pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository

import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementService

class HarvestSettlementRepositoryImpl @Inject constructor(
    private val service: HarvestSettlementService,
    private val dao: HarvestSettlementDao,
    private val apiCaller: ApiCaller,
) : HarvestSettlementRepository {

    override fun observeAll(): Flow<List<HarvestSettlement>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() }.sortedByDescending { it.settledAt } }

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        val remote = apiCaller.call { service.getSettlements(plotId) }
        if (remote is AppResult.Failure) return remote
        val entities = (remote as AppResult.Success).value.map { it.toEntity() }
        return try {
            dao.replaceForPlot(plotId, entities)
            AppResult.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unknown(throwable))
        }
    }

    override suspend fun refreshAll(plotIds: List<String>): AppResult<Unit> {
        val results = plotIds.map { refresh(it) }
        return results.firstOrNull { it is AppResult.Failure } ?: AppResult.Success(Unit)
    }
}
