package pe.edu.upc.viora.features.phenology.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord

interface HarvestRecordRepository {
    /** Emits the cached harvest records of the plot, newest campaign first. */
    fun observeRecords(plotId: String): Flow<List<HarvestRecord>>

    /** Emits the cached server bearing index, or null when the plot has none yet. */
    fun observeBearingIndex(plotId: String): Flow<BearingIndex?>

    /** Emits when the plot's history was last refreshed from the server, null if never. */
    fun observeLastRefresh(plotId: String): Flow<Instant?>

    /** Fetches records and bearing index from the server and replaces the plot's cache. */
    suspend fun refresh(plotId: String): AppResult<Unit>

    /** Registers the yield of a past campaign. Needs a connection. */
    suspend fun record(plotId: String, campaignYear: Int, totalYieldKg: Double): AppResult<HarvestRecord>

    /** Corrects the yield of a registered campaign. Needs a connection. */
    suspend fun rectify(plotId: String, recordId: String, totalYieldKg: Double): AppResult<HarvestRecord>

    /** Deletes a registered campaign. Needs a connection. */
    suspend fun remove(plotId: String, recordId: String): AppResult<Unit>
}
