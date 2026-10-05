package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for local caching of agroclimatic incidents.
 * Provides reactive queries for the Alerts Center and Home screen.
 */
@Dao
interface IncidentDao {

    /** Emits all cached incidents ordered by detection time descending. */
    @Query("SELECT * FROM agroclimatic_incidents ORDER BY triggered_at DESC")
    fun observeAll(): Flow<List<IncidentEntity>>

    /** Emits incidents scoped to a specific plot ordered by detection time descending. */
    @Query("SELECT * FROM agroclimatic_incidents WHERE plot_id = :plotId ORDER BY triggered_at DESC")
    fun observeByPlot(plotId: String): Flow<List<IncidentEntity>>

    /** Finds a single cached incident by its unique ID. */
    @Query("SELECT * FROM agroclimatic_incidents WHERE id = :id")
    suspend fun getById(id: String): IncidentEntity?

    /** Upserts a batch of incidents synchronized from the remote backend. */
    @Upsert
    suspend fun upsertAll(entities: List<IncidentEntity>)

    /** Drops cached incidents no longer present on the server. */
    @Query("DELETE FROM agroclimatic_incidents WHERE id NOT IN (:keepIds)")
    suspend fun deleteExcept(keepIds: List<String>)

    /** Drops cached incidents for a plot that are no longer present on the server. */
    @Query("DELETE FROM agroclimatic_incidents WHERE plot_id = :plotId AND id NOT IN (:keepIds)")
    suspend fun deleteExceptForPlot(plotId: String, keepIds: List<String>)

    /** Clears all cached incidents. */
    @Query("DELETE FROM agroclimatic_incidents")
    suspend fun deleteAll()

    /** Updates local status and snooze timestamp when an alert is postponed offline or optimistically. */
    @Query("UPDATE agroclimatic_incidents SET status = :status, snoozed_until = :snoozedUntil WHERE id = :incidentId")
    suspend fun updatePostponed(incidentId: String, status: String, snoozedUntil: String?)
}
