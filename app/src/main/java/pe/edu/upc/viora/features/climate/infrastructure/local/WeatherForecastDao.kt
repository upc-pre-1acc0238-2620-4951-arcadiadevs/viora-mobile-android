package pe.edu.upc.viora.features.climate.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherForecastDao {

    @Query("SELECT * FROM weather_forecasts WHERE plot_id = :plotId ORDER BY forecast_date ASC")
    fun observeByPlot(plotId: String): Flow<List<WeatherForecastEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<WeatherForecastEntity>)

    @Query("DELETE FROM weather_forecasts WHERE plot_id = :plotId")
    suspend fun deleteByPlot(plotId: String)

    /**
     * Atomically replaces the cached forecast rows for [plotId] so stale dates disappear.
     */
    @Transaction
    suspend fun replaceForPlot(plotId: String, entities: List<WeatherForecastEntity>) {
        deleteByPlot(plotId)
        upsertAll(entities)
    }
}
