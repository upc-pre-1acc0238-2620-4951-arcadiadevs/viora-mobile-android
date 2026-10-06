package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ForecastDayDao {

    @Query("SELECT * FROM forecast_days WHERE plot_id = :plotId ORDER BY forecast_date ASC")
    fun observeByPlot(plotId: String): Flow<List<ForecastDayEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<ForecastDayEntity>)

    @Query("DELETE FROM forecast_days WHERE plot_id = :plotId")
    suspend fun deleteByPlot(plotId: String)

    /** Replaces the whole cached forecast atomically so days that are over disappear. */
    @Transaction
    suspend fun replaceForPlot(plotId: String, entities: List<ForecastDayEntity>) {
        deleteByPlot(plotId)
        upsertAll(entities)
    }
}
