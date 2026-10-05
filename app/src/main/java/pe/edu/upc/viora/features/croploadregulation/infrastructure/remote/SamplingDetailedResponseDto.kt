package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Response from `GET /api/v1/plots/{plotId}/samplings?view=detailed`.
 */
@Serializable
data class SamplingDetailedResponseDto(
    val plotId: String,
    val campaignYear: Int,
    val sampledTreesCount: Int,
    val sampledShootsCount: Int,
    val sampledFruitSetCount: Int,
    val meanFruitsPerShoot: Double,
    val isRepresentative: Boolean,
    val treesNeeded: Int,
    val loadUnit: String = "FRUITS_PER_SHOOT",
    val trees: List<SamplingTreeItemDto> = emptyList(),
)
