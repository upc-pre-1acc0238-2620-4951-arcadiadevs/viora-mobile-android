package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Cached copy of an agroclimatic incident in the Room database. */
@Entity(tableName = "agroclimatic_incidents")
data class IncidentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "plot_name")
    val plotName: String,
    @ColumnInfo(name = "plot_variety")
    val plotVariety: String,
    val type: String,
    val severity: String,
    val status: String,
    @ColumnInfo(name = "headline_key")
    val headlineKey: String,
    @ColumnInfo(name = "metric_name")
    val metricName: String,
    @ColumnInfo(name = "current_value")
    val currentValue: Double,
    @ColumnInfo(name = "threshold_value")
    val thresholdValue: Double,
    val unit: String,
    @ColumnInfo(name = "triggered_at")
    val triggeredAt: String,
    @ColumnInfo(name = "stress_duration_minutes")
    val stressDurationMinutes: Long,
    @ColumnInfo(name = "snoozed_until")
    val snoozedUntil: String? = null,
)
