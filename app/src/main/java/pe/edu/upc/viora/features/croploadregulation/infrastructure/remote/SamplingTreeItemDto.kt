package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Individual evaluated tree item inside [SamplingDetailedResponseDto].
 */
@Serializable
data class SamplingTreeItemDto(
    val roundId: String,
    val treeTag: String,
    val shootCount: Int,
    val fruitSetCount: Int,
    val trunkDiameterMm: Double? = null,
    val samplingDate: String, // "YYYY-MM-DD"
)
