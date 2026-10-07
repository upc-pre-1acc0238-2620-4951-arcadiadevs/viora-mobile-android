package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Payload to record one tree sample evaluated in the orchard (P52 / P53).
 * [trunkDiameterMm] is optional and can be null when not measured in the field.
 */
@Serializable
data class TreeSampleRequestDto(
    val treeTag: String,
    val shootCount: Int,
    val fruitSetCount: Int,
    val trunkDiameterMm: Double? = null,
    val samplingDate: String, // "YYYY-MM-DD"
)
