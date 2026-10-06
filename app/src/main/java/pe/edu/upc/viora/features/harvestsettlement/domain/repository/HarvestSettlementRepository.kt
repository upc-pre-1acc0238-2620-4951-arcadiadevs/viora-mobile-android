package pe.edu.upc.viora.features.harvestsettlement.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSyncResult

interface HarvestSettlementRepository {
    /** Emits the cached settlements of every plot, newest `settledAt` first. */
    fun observeAll(): Flow<List<HarvestSettlement>>

    /** Fetches the plot's settlements and replaces its cache; settlements gone from the server disappear. */
    suspend fun refresh(plotId: String): AppResult<Unit>

    /**
     * Refreshes every plot. A failing plot never stops the others nor discards their data: all
     * plots are attempted and the first failure (in [plotIds] order) is returned, or success
     * when every plot refreshed.
     */
    suspend fun refreshAll(plotIds: List<String>): AppResult<Unit>

    /**
     * Settles the plot's harvest for the campaign. The `Idempotency-Key` is stable per draft: the
     * key of the plot/campaign's pending row if there is one, else [idempotencyKey], else a new
     * random one. On success the settlement is cached and any pending row is removed. A 409 is not
     * a failure: it returns [SettleOutcome.AlreadySettled]. Offline and timeouts save the draft as a
     * pending settlement, schedule the sync and return [SettleOutcome.Queued]. Other errors are
     * returned as failures and nothing is saved.
     */
    suspend fun settle(draft: SettleHarvestDraft, idempotencyKey: String? = null): AppResult<SettleOutcome>

    /** Emits every pending settlement (including FAILED and CONFLICT ones), oldest first. */
    fun observePending(): Flow<List<PendingSettlement>>

    /**
     * Replaces the data of the pending settlement of the draft's plot and campaign, keeping its
     * idempotency key, resets it to PENDING and schedules the sync. Fails with `NotFound` when
     * there is no pending row (it already synced, or was discarded).
     */
    suspend fun updatePending(draft: SettleHarvestDraft): AppResult<Unit>

    /** Deletes the pending settlement of the plot and campaign; a no-op when there is none. */
    suspend fun discardPending(plotId: String, campaignYear: Int): AppResult<Unit>

    /**
     * Sends every PENDING settlement with its stored key. Settled rows are deleted, 409s become
     * CONFLICT, 400/403/404/422 become FAILED and are not retried. Returns [SettlementSyncResult.RETRY_LATER]
     * when something could not be sent for a transient reason (offline, timeout, 5xx).
     */
    suspend fun syncPending(): SettlementSyncResult
}
