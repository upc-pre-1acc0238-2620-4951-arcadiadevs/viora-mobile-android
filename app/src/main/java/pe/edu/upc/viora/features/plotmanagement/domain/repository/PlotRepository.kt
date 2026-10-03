package pe.edu.upc.viora.features.plotmanagement.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/**
 * Offline-first access to the producer's plots. The local cache is the source of truth:
 * screens observe it and call [refresh] to pull the latest state from the server.
 */
interface PlotRepository {

    /** Active plots from the local cache, ordered by name. Emits again after every refresh. */
    fun observePlots(): Flow<List<Plot>>

    /**
     * One plot from the local cache, or `null` while it is not cached. Emits again when the plot
     * changes, e.g. after a refresh.
     */
    fun observePlot(id: PlotId): Flow<Plot?>

    /** When the plot list was last downloaded successfully, or `null` if never. */
    fun observeLastRefresh(): Flow<Instant?>

    /** Downloads the plot list and replaces the cache. The cache is untouched on failure. */
    suspend fun refresh(): AppResult<Unit>

    /**
     * Registers a new plot. On success the plot is also stored in the local cache, so the list
     * shows it immediately. Fails with `Conflict` when the name is already used.
     */
    suspend fun register(newPlot: NewPlot): AppResult<Plot>
}
