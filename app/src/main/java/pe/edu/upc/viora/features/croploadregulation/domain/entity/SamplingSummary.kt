package pe.edu.upc.viora.features.croploadregulation.domain.entity

import java.time.Instant

/**
 * Statistical summary of the accumulated sampling in an orchard plot.
 */
data class SamplingSummary(
    val plotId: String,
    val campaignYear: Int,
    val evaluatedTreesCount: Int,
    val sampledShootsCount: Int,
    val sampledFruitSetCount: Int,
    val meanFruitsPerShoot: Double,
    val isRepresentative: Boolean,
    val treesNeeded: Int,
    val loadUnit: String = "FRUITS_PER_SHOOT",
    val prescriptionId: String? = null,
    val status: String? = null,
    val targetTreesCount: Int = evaluatedTreesCount + treesNeeded,
    val coveragePercentage: Double = if (targetTreesCount > 0) (evaluatedTreesCount.toDouble() / targetTreesCount) * 100.0 else 0.0,
    val updatedAt: Instant? = null,
)
