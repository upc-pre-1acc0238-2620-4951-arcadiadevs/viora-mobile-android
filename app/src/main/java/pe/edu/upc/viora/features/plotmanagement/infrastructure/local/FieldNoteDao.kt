package pe.edu.upc.viora.features.plotmanagement.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldNoteDao {

    @Query("SELECT * FROM field_notes ORDER BY observed_on DESC, created_at DESC")
    fun observeAll(): Flow<List<FieldNoteEntity>>

    @Query("SELECT * FROM field_notes WHERE sync_state = 'PENDING' OR photo_pending = 1 ORDER BY created_at")
    suspend fun findUnsent(): List<FieldNoteEntity>

    @Query("SELECT * FROM field_notes WHERE id = :id LIMIT 1")
    suspend fun find(id: String): FieldNoteEntity?

    @Upsert
    suspend fun upsert(entity: FieldNoteEntity)

    @Query("DELETE FROM field_notes WHERE plot_id = :plotId AND sync_state = 'SYNCED' AND photo_pending = 0")
    suspend fun deleteSyncedOfPlot(plotId: String)

    @Upsert
    suspend fun upsertAll(entities: List<FieldNoteEntity>)

    /** Replaces what the server holds for a plot, keeping the notes still waiting on the phone. */
    @Transaction
    suspend fun replaceSyncedOfPlot(plotId: String, notes: List<FieldNoteEntity>) {
        deleteSyncedOfPlot(plotId)
        val unsent = findUnsent().map { it.id }.toSet()
        upsertAll(notes.filter { it.id !in unsent })
    }

    /** Signing out (or another account signing in): the whole table goes, see the context's tables wiper. */
    @Query("DELETE FROM field_notes")
    suspend fun wipeAll()
}
