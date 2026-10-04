package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {

    @Query("SELECT * FROM agroclimatic_incidents ORDER BY triggered_at DESC")
    fun observeAll(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM agroclimatic_incidents WHERE plot_id = :plotId ORDER BY triggered_at DESC")
    fun observeByPlot(plotId: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM agroclimatic_incidents WHERE id = :id")
    suspend fun getById(id: String): IncidentEntity?

    @Upsert
    suspend fun upsertAll(entities: List<IncidentEntity>)

    @Query("DELETE FROM agroclimatic_incidents WHERE id NOT IN (:keepIds)")
    suspend fun deleteExcept(keepIds: List<String>)

    @Query("DELETE FROM agroclimatic_incidents WHERE plot_id = :plotId AND id NOT IN (:keepIds)")
    suspend fun deleteExceptForPlot(plotId: String, keepIds: List<String>)

    @Query("DELETE FROM agroclimatic_incidents")
    suspend fun deleteAll()

    @Query("UPDATE agroclimatic_incidents SET status = :status, snoozed_until = :snoozedUntil WHERE id = :incidentId")
    suspend fun updatePostponed(incidentId: String, status: String, snoozedUntil: String?)
}
