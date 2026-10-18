package pe.edu.upc.viora.features.croploadregulation.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity

/**
 * Cached thinning plan of a plot and campaign (P61), so it can be read without signal. The plan is stored as the JSON
 * the backend sent because it is only read whole; [payloadJson] is null when the plot has no plan for the campaign.
 * [executionJson] is the thinning recorded against the plan (P63), null until it is recorded.
 */
@Entity(tableName = "thinning_prescriptions", primaryKeys = ["plot_id", "campaign_year"])
data class ThinningPrescriptionEntity(
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "campaign_year")
    val campaignYear: Int,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String?,
    @ColumnInfo(name = "synced_at_epoch_ms")
    val syncedAtEpochMs: Long,
    @ColumnInfo(name = "execution_json")
    val executionJson: String? = null,
)
