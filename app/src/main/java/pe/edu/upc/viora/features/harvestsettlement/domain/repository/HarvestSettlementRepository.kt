package pe.edu.upc.viora.features.harvestsettlement.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement

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
}
