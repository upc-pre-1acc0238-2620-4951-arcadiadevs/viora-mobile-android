package pe.edu.upc.viora.features.phenology.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChillTrackerDao {

    @Query("SELECT * FROM chill_trackers WHERE plot_id = :plotId")
    fun observeByPlot(plotId: String): Flow<ChillTrackerEntity?>

    @Upsert
    suspend fun upsert(entity: ChillTrackerEntity)

    @Query("DELETE FROM chill_trackers WHERE plot_id = :plotId")
    suspend fun deleteByPlot(plotId: String)
}
