package pe.edu.upc.viora.features.phenology.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker

interface ChillRepository {

    /** Emits cached chilling tracker for the plot, or null if not yet cached. */
    fun observeChillTracker(plotId: String): Flow<ChillTracker?>

    /** Emits the last sync instant for the plot's chilling data, or null if never synced. */
    fun observeLastSync(plotId: String): Flow<Instant?>

    /** Fetches the latest chilling metrics from the server and updates the local Room cache. */
    suspend fun refresh(plotId: String): AppResult<Unit>
}
