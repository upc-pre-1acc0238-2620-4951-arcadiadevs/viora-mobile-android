package pe.edu.upc.viora.features.croploadregulation.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "draft_tree_samples")
data class DraftTreeSampleEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "plot_name")
    val plotName: String,
    @ColumnInfo(name = "campaign_year")
    val campaignYear: Int,
    @ColumnInfo(name = "tree_identifier")
    val treeIdentifier: String,
    @ColumnInfo(name = "shoots_count")
    val shootsCount: Int,
    @ColumnInfo(name = "fruit_set_count")
    val fruitSetCount: Int,
    @ColumnInfo(name = "trunk_circumference_cm")
    val trunkCircumferenceCm: Double? = null,
    @ColumnInfo(name = "trunk_diameter_mm")
    val trunkDiameterMm: Double? = null,
    @ColumnInfo(name = "observed_on")
    val observedOn: String,
    @ColumnInfo(name = "is_synced", defaultValue = "0")
    val isSynced: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    /**
     * The client batch id this draft is sent with, every time: a retry of a batch the server already took is then a
     * replay it ignores, never a second round. Null only for drafts saved before version 15 of the database.
     */
    @ColumnInfo(name = "batch_id")
    val batchId: String? = null,
)
