package pe.edu.upc.viora.features.croploadregulation.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ThinningEventDao {

    @Query("SELECT * FROM thinning_events ORDER BY occurred_at DESC")
    fun observeAll(): Flow<List<ThinningEventEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<ThinningEventEntity>)

    @Query("DELETE FROM thinning_events")
    suspend fun deleteAll()

    /** Replaces the cached feed atomically, so events the server no longer returns disappear. */
    @Transaction
    suspend fun replaceAll(entities: List<ThinningEventEntity>) {
        deleteAll()
        upsertAll(entities)
    }
}
