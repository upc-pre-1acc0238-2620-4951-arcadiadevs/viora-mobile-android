package pe.edu.upc.viora.features.croploadregulation.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Cached row of `GET /thinning-events` (sampling milestones and executed thinnings) so the
 * logbook (P50) can draw its timeline before the network answers. Dates are ISO-8601 text.
 */
@Entity(tableName = "thinning_events")
data class ThinningEventEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "event_type")
    val eventType: String,
    @ColumnInfo(name = "prescription_id")
    val prescriptionId: String,
    @ColumnInfo(name = "confirmation_id")
    val confirmationId: String?,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "plot_name")
    val plotName: String,
    @ColumnInfo(name = "campaign_year")
    val campaignYear: Int,
    @ColumnInfo(name = "occurred_at")
    val occurredAt: String,
    @ColumnInfo(name = "evaluated_trees_count")
    val evaluatedTreesCount: Int?,
    @ColumnInfo(name = "total_shoots_count")
    val totalShootsCount: Int?,
    @ColumnInfo(name = "total_fruits_count")
    val totalFruitsCount: Int?,
    @ColumnInfo(name = "mean_fruits_per_shoot")
    val meanFruitsPerShoot: Double?,
    @ColumnInfo(name = "is_representative")
    val isRepresentative: Boolean?,
    @ColumnInfo(name = "removal_percentage")
    val removalPercentage: Double?,
    @ColumnInfo(name = "removed_kg")
    val removedKg: Double?,
    @ColumnInfo(name = "executed_date")
    val executedDate: String?,
    @ColumnInfo(name = "labor_crew_size")
    val laborCrewSize: Int?,
    val timeliness: String?,
)
