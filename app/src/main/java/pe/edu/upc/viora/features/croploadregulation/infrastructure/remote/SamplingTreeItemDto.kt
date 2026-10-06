package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Individual evaluated tree item inside [SamplingDetailedResponseDto].
 */
@Serializable
data class SamplingTreeItemDto(
    val roundId: String? = null,
    val treeTag: String = "",
    val shootCount: Int = 0,
    val fruitSetCount: Int = 0,
    val trunkDiameterMm: Double? = null,
    val samplingDate: String = "", // "YYYY-MM-DD"
)
