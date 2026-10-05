package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorNodeDao {

    @Query("SELECT * FROM sensor_nodes WHERE plot_id = :plotId ORDER BY name COLLATE NOCASE ASC")
    fun observeByPlot(plotId: String): Flow<List<SensorNodeEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<SensorNodeEntity>)

    @Query("DELETE FROM sensor_nodes WHERE plot_id = :plotId AND id NOT IN (:keepIds)")
    suspend fun deleteExcept(plotId: String, keepIds: List<String>)

    @Query("DELETE FROM sensor_nodes WHERE plot_id = :plotId AND id = :nodeId")
    suspend fun delete(plotId: String, nodeId: String)
}
