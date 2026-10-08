package pe.edu.upc.viora.features.phenology.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Cached winter chill of a plot (Dynamic Model, US22/US23). Dates are ISO strings; the 92-day curve is stored as
 * JSON because it is only read whole, to draw the chart.
 */
@Entity(tableName = "chill_trackers")
data class ChillTrackerEntity(
    @PrimaryKey
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "season_year")
    val seasonYear: Int,
    @ColumnInfo(name = "accumulated_portions")
    val accumulatedPortions: Double,
    @ColumnInfo(name = "threshold_portions")
    val thresholdPortions: Double,
    @ColumnInfo(name = "season_state")
    val seasonState: String,
    @ColumnInfo(name = "evaluated_through")
    val evaluatedThrough: String?,
    @ColumnInfo(name = "completion_date")
    val completionDate: String?,
    @ColumnInfo(name = "projection_status")
    val projectionStatus: String,
    @ColumnInfo(name = "projected_completion_date")
    val projectedCompletionDate: String?,
    @ColumnInfo(name = "days_above_24_celsius")
    val daysAbove24Celsius: Int,
    @ColumnInfo(name = "current_warm_streak_days")
    val currentWarmStreakDays: Int,
    @ColumnInfo(name = "longest_warm_streak_days")
    val longestWarmStreakDays: Int,
    @ColumnInfo(name = "thermal_anomaly")
    val thermalAnomaly: String,
    @ColumnInfo(name = "previous_season_year")
    val previousSeasonYear: Int?,
    @ColumnInfo(name = "previous_season_portions")
    val previousSeasonPortions: Double?,
    @ColumnInfo(name = "previous_season_completion_date")
    val previousSeasonCompletionDate: String?,
    @ColumnInfo(name = "curve_points_json")
    val curvePointsJson: String,
    @ColumnInfo(name = "synced_at_epoch_ms")
    val syncedAtEpochMs: Long,
)
