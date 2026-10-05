package pe.edu.upc.viora.features.harvestsettlement.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestSettlementDao {

    @Query("SELECT * FROM harvest_settlements")
    fun observeAll(): Flow<List<HarvestSettlementEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<HarvestSettlementEntity>)

    @Query("DELETE FROM harvest_settlements WHERE plot_id = :plotId")
    suspend fun deleteByPlot(plotId: String)

    /** Replaces every cached settlement of the plot atomically so deleted ones disappear. */
    @Transaction
    suspend fun replaceForPlot(plotId: String, entities: List<HarvestSettlementEntity>) {
        deleteByPlot(plotId)
        upsertAll(entities)
    }
}
