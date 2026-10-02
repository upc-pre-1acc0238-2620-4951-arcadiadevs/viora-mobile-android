package pe.edu.upc.viora.features.plotmanagement.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Cached copy of a plot. Enums and dates are stored as the same strings the API sends. */
@Entity(tableName = "plots")
data class PlotEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val variety: String,
    @ColumnInfo(name = "area_ha")
    val areaHa: Double,
    @ColumnInfo(name = "tree_density")
    val treeDensity: Int,
    @ColumnInfo(name = "row_spacing_m")
    val rowSpacingM: Double,
    @ColumnInfo(name = "tree_spacing_m")
    val treeSpacingM: Double,
    @ColumnInfo(name = "polygon_geojson")
    val polygonGeoJson: String,
    @ColumnInfo(name = "last_pruning_date")
    val lastPruningDate: String?,
    val status: String,
    val revision: Long,
)
