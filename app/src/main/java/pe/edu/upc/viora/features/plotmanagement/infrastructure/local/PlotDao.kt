package pe.edu.upc.viora.features.plotmanagement.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlotDao {

    @Query("SELECT * FROM plots WHERE status = 'ACTIVE' ORDER BY name COLLATE NOCASE ASC")
    fun observeActive(): Flow<List<PlotEntity>>

    @Query("SELECT * FROM plots WHERE status != 'ACTIVE' ORDER BY name COLLATE NOCASE ASC")
    fun observeArchived(): Flow<List<PlotEntity>>

    /** One plot whatever its status, or `null` when it is not cached. Emits again when it changes. */
    @Query("SELECT * FROM plots WHERE id = :id")
    fun observeById(id: String): Flow<PlotEntity?>

    /** Marks a plot as archived, as the server did when it was archived (it also bumps its revision). */
    @Query("UPDATE plots SET status = 'REMOVED_SOFT_DELETE', revision = revision + 1 WHERE id = :id")
    suspend fun markArchived(id: String)

    @Upsert
    suspend fun upsertAll(entities: List<PlotEntity>)

    /** Removes plots the server no longer returns (deleted or archived elsewhere). */
    /** Drops the active plots the server no longer lists (archived elsewhere); archived ones are kept. */
    @Query("DELETE FROM plots WHERE status = 'ACTIVE' AND id NOT IN (:keepIds)")
    suspend fun deleteActiveExcept(keepIds: List<String>)

    /** Drops the archived plots the server no longer lists (restored elsewhere). */
    @Query("DELETE FROM plots WHERE status != 'ACTIVE' AND id NOT IN (:keepIds)")
    suspend fun deleteArchivedExcept(keepIds: List<String>)
}
