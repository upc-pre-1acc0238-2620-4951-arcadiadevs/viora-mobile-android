package pe.edu.upc.viora.features.phenology.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Cached copy of a harvest record in the Room database. */
@Entity(tableName = "harvest_records")
data class HarvestRecordEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "campaign_year")
    val campaignYear: Int,
    @ColumnInfo(name = "total_yield_kg")
    val totalYieldKg: Double,
    @ColumnInfo(name = "green_kg")
    val greenKg: Double?,
    @ColumnInfo(name = "black_kg")
    val blackKg: Double?,
    val bearing: String,
    @ColumnInfo(name = "recorded_at")
    val recordedAt: String?,
)
