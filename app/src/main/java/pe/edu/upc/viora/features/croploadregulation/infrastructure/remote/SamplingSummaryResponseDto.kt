package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Response from `GET /api/v1/plots/{plotId}/samplings/summary` and `POST /api/v1/plots/{plotId}/samplings`.
 */
@Serializable
data class SamplingSummaryResponseDto(
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
    val updatedAt: String,
)
