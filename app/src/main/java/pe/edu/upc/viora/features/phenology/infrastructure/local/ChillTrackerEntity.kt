package pe.edu.upc.viora.features.phenology.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Cached chilling tracker data of a plot (Erez model, US22). */
@Entity(tableName = "chill_trackers")
data class ChillTrackerEntity(
    @PrimaryKey
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "accumulated_portions")
    val accumulatedPortions: Double,
    @ColumnInfo(name = "threshold_portions")
    val thresholdPortions: Double,
    @ColumnInfo(name = "days_above_24_celsius")
    val daysAbove24Celsius: Int,
    @ColumnInfo(name = "season_state")
    val seasonState: String,
    @ColumnInfo(name = "projected_completion_date")
    val projectedCompletionDate: String?,
    @ColumnInfo(name = "previous_winter_completion_date")
    val previousWinterCompletionDate: String?,
    @ColumnInfo(name = "enso_risk")
    val ensoRisk: String,
    @ColumnInfo(name = "curve_points_json")
    val curvePointsJson: String,
    @ColumnInfo(name = "synced_at_epoch_ms")
    val syncedAtEpochMs: Long,
)
