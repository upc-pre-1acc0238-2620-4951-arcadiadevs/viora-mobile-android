package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Item in the timeline returned by `GET /api/v1/thinning-events`.
 * [eventType] is either "SAMPLING_COMPLETED" or "THINNING_EXECUTED".
 */
@Serializable
data class ThinningEventItemDto(
    val id: String,
    val eventType: String,
    val prescriptionId: String,
    val confirmationId: String? = null,
    val plotId: String,
    val plotName: String,
    val campaignYear: Int,
    val occurredAt: String, // ISO-8601 string
    // Properties present when SAMPLING_COMPLETED
    val evaluatedTreesCount: Int? = null,
    val totalShootsCount: Int? = null,
    val totalFruitsCount: Int? = null,
    val meanFruitsPerShoot: Double? = null,
    val isRepresentative: Boolean? = null,
    // Properties present when THINNING_EXECUTED
    val removalPercentage: Double? = null,
    val removedKg: Double? = null,
    val executedDate: String? = null,
    val laborCrewSize: Int? = null,
    val timeliness: String? = null,
)
