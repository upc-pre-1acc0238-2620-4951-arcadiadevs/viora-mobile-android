package pe.edu.upc.viora.features.plotmanagement.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
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

    /** Archived plots from the local cache, ordered by name. */
    fun observeArchivedPlots(): Flow<List<Plot>>

    /** When the plot list was last downloaded successfully, or `null` if never. */
    fun observeLastRefresh(): Flow<Instant?>

    /** Downloads the plot list and replaces the cache. The cache is untouched on failure. */
    suspend fun refresh(): AppResult<Unit>

    /** Reads the archived plots from the server into the cache. */
    suspend fun refreshArchived(): AppResult<Unit>

    /**
     * Registers a new plot. On success the plot is also stored in the local cache, so the list
     * shows it immediately. Fails with `Conflict` when the name is already used.
     */
    suspend fun register(newPlot: NewPlot): AppResult<Plot>

    /**
     * Edits the plot with [id]. The request carries the revision the cache holds; if the plot
     * changed elsewhere meanwhile the result is [pe.edu.upc.viora.core.domain.AppError.PreconditionFailed]
     * and the cache is refreshed so the producer sees the current data.
     */
    suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot>

    /** Archives the plot: it disappears from the active list but its history is kept by the backend. */
    suspend fun archive(id: PlotId): AppResult<Unit>

    /** Brings an archived plot back to the active ones, history included. Needs the connection. */
    suspend fun restore(id: PlotId): AppResult<Plot>
}
