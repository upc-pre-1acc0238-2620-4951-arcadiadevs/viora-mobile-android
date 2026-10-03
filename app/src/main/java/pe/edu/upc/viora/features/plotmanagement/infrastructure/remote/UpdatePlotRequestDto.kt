package pe.edu.upc.viora.features.plotmanagement.infrastructure.remote

import kotlinx.serialization.Serializable

/** Body of `PUT plots/{id}`. The backend replaces the whole editable state, so every field is sent. */
@Serializable
data class UpdatePlotRequestDto(
    val name: String,
    val variety: String,
    val rowSpacingM: Double,
    val treeSpacingM: Double,
    val lastPruningDate: String? = null,
    val polygonGeoJson: String,
)
