package pe.edu.upc.viora.features.phenology.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Cached server bearing index of a plot; at most one row per plot. */
@Entity(tableName = "bearing_indexes")
data class BearingIndexEntity(
    @PrimaryKey
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    val value: Double?,
    @ColumnInfo(name = "evaluated_years")
    val evaluatedYears: Int,
    @ColumnInfo(name = "evaluated_at")
    val evaluatedAt: String?,
)
