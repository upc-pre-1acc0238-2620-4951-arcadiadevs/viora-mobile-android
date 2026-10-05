package pe.edu.upc.viora.features.phenology.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BearingIndexDao {

    @Query("SELECT * FROM bearing_indexes WHERE plot_id = :plotId")
    fun observeByPlot(plotId: String): Flow<BearingIndexEntity?>

    @Upsert
    suspend fun upsert(entity: BearingIndexEntity)

    @Query("DELETE FROM bearing_indexes WHERE plot_id = :plotId")
    suspend fun deleteByPlot(plotId: String)
}
