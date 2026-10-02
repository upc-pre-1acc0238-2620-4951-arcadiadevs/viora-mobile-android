package pe.edu.upc.viora.features.plotmanagement.infrastructure.remote

import kotlinx.serialization.Serializable

/** `PlotResource` of the backend (`/api/v1/plots`). Dates stay ISO-8601 strings here. */
@Serializable
data class PlotDto(
    val id: String,
    val producerId: String,
    val name: String,
    val variety: String,
    val areaHa: Double,
    val treeDensity: Int,
    val rowSpacingM: Double,
    val treeSpacingM: Double,
    /** A GeoJSON `Polygon` serialized as a string (WGS84, `[longitude, latitude]` pairs). */
    val polygonGeoJson: String,
    val lastPruningDate: String? = null,
    val status: String,
    val revision: Long,
)
