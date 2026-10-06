package pe.edu.upc.viora.features.harvestsettlement.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome

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
     * Settles the plot's harvest for the campaign, sending [idempotencyKey] so a retry never
     * creates a second settlement. On success the settlement is cached. A 409 is not a failure:
     * it returns [SettleOutcome.AlreadySettled]. Offline, timeouts and other errors are returned
     * as failures and nothing is cached.
     */
    suspend fun settle(draft: SettleHarvestDraft, idempotencyKey: String): AppResult<SettleOutcome>
}
