package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryReadingDao {

    @Query(
        "SELECT * FROM telemetry_readings " +
            "WHERE plot_id = :plotId AND observed_at_epoch_ms >= :sinceEpochMs " +
            "ORDER BY observed_at_epoch_ms ASC",
    )
    fun observeSince(plotId: String, sinceEpochMs: Long): Flow<List<TelemetryReadingEntity>>

    @Upsert
    suspend fun upsertAll(entities: List<TelemetryReadingEntity>)

    /** Keeps the table small: nothing in the app reads further back than the 30-day window. */
    @Query("DELETE FROM telemetry_readings WHERE plot_id = :plotId AND observed_at_epoch_ms < :beforeEpochMs")
    suspend fun deleteBefore(plotId: String, beforeEpochMs: Long)
}
