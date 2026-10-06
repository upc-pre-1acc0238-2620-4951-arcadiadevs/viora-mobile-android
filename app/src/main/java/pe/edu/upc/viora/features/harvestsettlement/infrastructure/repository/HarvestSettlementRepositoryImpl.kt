package pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository

import java.time.Clock
import java.time.format.DateTimeParseException
import java.util.UUID
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
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSyncResult
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository
import pe.edu.upc.viora.features.harvestsettlement.domain.service.SettlementSyncScheduler
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.PendingSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.PendingSettlementEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toDraft
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toPendingEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toRequestDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.ExistingSettlementDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementService

class HarvestSettlementRepositoryImpl @Inject constructor(
    private val service: HarvestSettlementService,
    private val dao: HarvestSettlementDao,
    private val pendingDao: PendingSettlementDao,
    private val scheduler: SettlementSyncScheduler,
    private val apiCaller: ApiCaller,
    private val json: Json,
    private val clock: Clock,
) : HarvestSettlementRepository {

    override fun observeAll(): Flow<List<HarvestSettlement>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() }.sortedByDescending { it.settledAt } }

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        val remote = apiCaller.call { service.getSettlements(plotId) }
        if (remote is AppResult.Failure) return remote
        val entities = (remote as AppResult.Success).value.map { it.toEntity() }
        return guarded { dao.replaceForPlot(plotId, entities) }
    }

    override suspend fun settle(draft: SettleHarvestDraft, idempotencyKey: String?): AppResult<SettleOutcome> {
        val existing = guarded { pendingDao.find(draft.plotId, draft.campaignYear) }
        val pending = (existing as? AppResult.Success)?.value
        val key = pending?.idempotencyKey ?: idempotencyKey ?: UUID.randomUUID().toString()
        val remote = settleRemote(draft, key)
        if (remote is AppResult.Success) {
            if (pending != null) guarded { pendingDao.delete(draft.plotId, draft.campaignYear) }
            return remote
        }
        val error = (remote as AppResult.Failure).error
        return if (error is AppError.Offline || error is AppError.Timeout) queue(draft, key, pending) else remote
    }

    /** Sends the settlement without touching the pending queue; 409 becomes [SettleOutcome.AlreadySettled]. */
    private suspend fun settleRemote(draft: SettleHarvestDraft, idempotencyKey: String): AppResult<SettleOutcome> {
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

    /** Saves the draft as PENDING (replacing a previous row of the same plot/campaign), keeping its key. */
    private suspend fun queue(
        draft: SettleHarvestDraft,
        key: String,
        previous: PendingSettlementEntity?,
    ): AppResult<SettleOutcome> {
        val now = clock.millis()
        val entity = draft.toPendingEntity(key, createdAt = previous?.createdAt ?: now, updatedAt = now)
        val saved = guarded { pendingDao.upsert(entity) }
        if (saved is AppResult.Failure) return saved
        scheduler.schedule()
        return AppResult.Success(SettleOutcome.Queued(entity.toDomain()))
    }

    override fun observePending(): Flow<List<PendingSettlement>> =
        pendingDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun updatePending(draft: SettleHarvestDraft): AppResult<Unit> {
        val found = guarded { pendingDao.find(draft.plotId, draft.campaignYear) }
        if (found is AppResult.Failure) return found
        val previous = (found as AppResult.Success).value
            ?: return AppResult.Failure(AppError.NotFound("No pending settlement for the campaign"))
        val entity = draft.toPendingEntity(previous.idempotencyKey, previous.createdAt, clock.millis())
        val saved = guarded { pendingDao.upsert(entity) }
        if (saved is AppResult.Failure) return saved
        scheduler.schedule()
        return saved
    }

    override suspend fun discardPending(plotId: String, campaignYear: Int): AppResult<Unit> =
        guarded { pendingDao.delete(plotId, campaignYear) }

    override suspend fun syncPending(): SettlementSyncResult {
        val rows = try {
            pendingDao.findByStatus(PendingStatus.PENDING.name)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            return SettlementSyncResult.RETRY_LATER
        }
        var retryLater = false
        for (row in rows) {
            val transient = try {
                !syncRow(row)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                true
            }
            retryLater = retryLater || transient
        }
        return if (retryLater) SettlementSyncResult.RETRY_LATER else SettlementSyncResult.DONE
    }

    /** Sends one row and records the outcome; returns false when it must be retried later. */
    private suspend fun syncRow(row: PendingSettlementEntity): Boolean {
        val draft = try {
            row.toDraft()
        } catch (_: DateTimeParseException) {
            update(row) { it.copy(status = PendingStatus.FAILED.name, lastErrorCode = CORRUPT_ROW) }
            return true
        }
        val result = settleRemote(draft, row.idempotencyKey)
        if (result is AppResult.Success) {
            when (val outcome = result.value) {
                is SettleOutcome.Settled -> pendingDao.delete(row.plotId, row.campaignYear)
                is SettleOutcome.AlreadySettled -> update(row) {
                    it.copy(
                        status = PendingStatus.CONFLICT.name,
                        lastErrorCode = CONFLICT_CODE,
                        existingTotalYieldKg = outcome.existing?.totalYieldKg,
                        existingReceiptNumber = outcome.existing?.receiptNumber,
                        existingWeighedOn = outcome.existing?.weighedOn?.toString(),
                    )
                }
                is SettleOutcome.Queued -> Unit
            }
            return true
        }
        val error = (result as AppResult.Failure).error
        val permanentCode = permanentCodeOf(error)
        if (permanentCode != null) {
            update(row) { it.copy(status = PendingStatus.FAILED.name, lastErrorCode = permanentCode) }
            return true
        }
        update(row) { it.copy(attemptCount = it.attemptCount + 1, lastErrorCode = transientCodeOf(error)) }
        return false
    }

    /**
     * Writes the change only if the row was not edited meanwhile: an edit replaced it with a
     * newer `updatedAt` and has its own sync pass queued, so this stale result must not overwrite it.
     */
    private suspend fun update(row: PendingSettlementEntity, change: (PendingSettlementEntity) -> PendingSettlementEntity) {
        val current = pendingDao.find(row.plotId, row.campaignYear)
        if (current != null && current.updatedAt == row.updatedAt) pendingDao.upsert(change(current))
    }

    /** 400/422 (rejected data), 403 (not the owner) and 404 (plot gone) will not succeed on retry. */
    private fun permanentCodeOf(error: AppError): String? = when (error) {
        is AppError.Validation -> error.code ?: "VALIDATION_ERROR"
        is AppError.Forbidden -> "FORBIDDEN"
        is AppError.NotFound -> error.code ?: "NOT_FOUND"
        else -> null
    }

    private fun transientCodeOf(error: AppError): String = when (error) {
        AppError.Offline -> "OFFLINE"
        AppError.Timeout -> "TIMEOUT"
        is AppError.Server -> "SERVER_${error.status}"
        AppError.Unauthorized -> "UNAUTHORIZED"
        else -> "UNKNOWN"
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

    private suspend fun <T> guarded(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
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
        const val CONFLICT_CODE = "HARVESTSETTLEMENT_CONFLICT"
        const val CORRUPT_ROW = "CORRUPT_ROW"
    }
}
