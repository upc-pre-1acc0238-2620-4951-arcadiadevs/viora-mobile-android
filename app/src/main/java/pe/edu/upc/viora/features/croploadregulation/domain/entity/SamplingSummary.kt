package pe.edu.upc.viora.features.croploadregulation.domain.entity

import java.time.Instant

/**
 * Statistical summary of the accumulated sampling in an orchard plot.
 */
data class SamplingSummary(
    val prescriptionId: String,
    val plotId: String,
    val campaignYear: Int,
    val status: String,
    val evaluatedTreesCount: Int,
    val targetTreesCount: Int,
    val coveragePercentage: Double,
    val meanFruitsPerShoot: Double,
    val sampledFruitSetCount: Int,
    val isRepresentative: Boolean,
    val updatedAt: Instant,
)
