package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Response from `GET /api/v1/plots/{plotId}/samplings?view=detailed`.
 */
@Serializable
data class SamplingDetailedResponseDto(
    val plotId: String = "",
    val campaignYear: Int = 0,
    val sampledTreesCount: Int = 0,
    val sampledShootsCount: Int = 0,
    val sampledFruitSetCount: Int = 0,
    val meanFruitsPerShoot: Double = 0.0,
    val isRepresentative: Boolean = false,
    val treesNeeded: Int = 0,
    val loadUnit: String = "FRUITS_PER_SHOOT",
    val trees: List<SamplingTreeItemDto> = emptyList(),
)
