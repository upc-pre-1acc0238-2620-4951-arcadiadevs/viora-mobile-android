package pe.edu.upc.viora.features.plotmanagement.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PlotDao {

    @Query("SELECT * FROM plots WHERE status = 'ACTIVE' ORDER BY name COLLATE NOCASE ASC")
    fun observeActive(): Flow<List<PlotEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<PlotEntity>)

    /** Removes plots the server no longer returns (deleted or archived elsewhere). */
    @Query("DELETE FROM plots WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)
}
