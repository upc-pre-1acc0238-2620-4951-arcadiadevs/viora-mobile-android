package pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository

import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toRequestDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.ExistingSettlementDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementService

class HarvestSettlementRepositoryImpl @Inject constructor(
    private val service: HarvestSettlementService,
    private val dao: HarvestSettlementDao,
    private val apiCaller: ApiCaller,
    private val json: Json,
) : HarvestSettlementRepository {

    override fun observeAll(): Flow<List<HarvestSettlement>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() }.sortedByDescending { it.settledAt } }

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        val remote = apiCaller.call { service.getSettlements(plotId) }
        if (remote is AppResult.Failure) return remote
        val entities = (remote as AppResult.Success).value.map { it.toEntity() }
        return guarded { dao.replaceForPlot(plotId, entities) }
    }

    override suspend fun settle(draft: SettleHarvestDraft, idempotencyKey: String): AppResult<SettleOutcome> {
        val remote = apiCaller.call { service.settle(draft.plotId, idempotencyKey, draft.toRequestDto()) }
        if (remote is AppResult.Failure) {
            val error = remote.error
            return if (error is AppError.Conflict) alreadySettled(draft, error) else remote
        }
        val dto = (remote as AppResult.Success).value
        val stored = guarded { dao.upsertAll(listOf(dto.toEntity())) }
        if (stored is AppResult.Failure) return stored
        return AppResult.Success(SettleOutcome.Settled(dto.toDomain()))
    }

    /** Prefers the server's `existingSettlement`; falls back to the cached settlement of the campaign. */
    private suspend fun alreadySettled(
        draft: SettleHarvestDraft,
        conflict: AppError.Conflict,
    ): AppResult<SettleOutcome> {
        val fromServer = conflict.properties?.get(EXISTING_SETTLEMENT)?.let { element ->
            try {
                json.decodeFromJsonElement<ExistingSettlementDto>(element).toDomain()
            } catch (_: SerializationException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }
        val summary = fromServer ?: try {
            dao.findByPlotAndYear(draft.plotId, draft.campaignYear)?.toDomain()?.let {
                SettlementSummary(it.campaignYear, it.totalYieldKg, it.receiptNumber, it.weighedOn)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            null
        }
        return AppResult.Success(SettleOutcome.AlreadySettled(summary))
    }

    private suspend fun guarded(block: suspend () -> Unit): AppResult<Unit> = try {
        block()
        AppResult.Success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unknown(throwable))
    }

    override suspend fun refreshAll(plotIds: List<String>): AppResult<Unit> {
        val results = plotIds.map { refresh(it) }
        return results.firstOrNull { it is AppResult.Failure } ?: AppResult.Success(Unit)
    }

    private companion object {
        const val EXISTING_SETTLEMENT = "existingSettlement"
    }
}
