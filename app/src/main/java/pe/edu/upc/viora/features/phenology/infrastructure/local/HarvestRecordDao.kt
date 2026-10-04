package pe.edu.upc.viora.features.phenology.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestRecordDao {

    @Query("SELECT * FROM harvest_records WHERE plot_id = :plotId ORDER BY campaign_year DESC")
    fun observeByPlot(plotId: String): Flow<List<HarvestRecordEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<HarvestRecordEntity>)

    @Query("DELETE FROM harvest_records WHERE plot_id = :plotId")
    suspend fun deleteByPlot(plotId: String)

    /** Replaces every cached record of the plot atomically so deleted ones disappear. */
    @Transaction
    suspend fun replaceForPlot(plotId: String, entities: List<HarvestRecordEntity>) {
        deleteByPlot(plotId)
        upsertAll(entities)
    }
}
