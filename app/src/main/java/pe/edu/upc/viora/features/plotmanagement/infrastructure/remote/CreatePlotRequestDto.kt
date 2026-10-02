package pe.edu.upc.viora.features.plotmanagement.infrastructure.remote

import kotlinx.serialization.Serializable

/** Body of `POST /api/v1/plots` (`CreatePlotResource` of the backend). */
@Serializable
data class CreatePlotRequestDto(
    val name: String,
    /** One of CRIOLLA, SEVILLANA, MANZANILLA, ARBEQUINA. */
    val variety: String,
    /** A GeoJSON `Polygon` as a string, closed ring of `[longitude, latitude]`. */
    val polygonGeoJson: String,
    val rowSpacingM: Double,
    val treeSpacingM: Double,
)
