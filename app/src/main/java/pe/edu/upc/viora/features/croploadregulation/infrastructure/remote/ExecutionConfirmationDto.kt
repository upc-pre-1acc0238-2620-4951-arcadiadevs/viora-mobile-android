package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Body of `POST /thinning-prescriptions/{id}/execution-confirmations` (P62). Kilos and crew are optional in the
 * backend and the producer does not give them, so they are not sent.
 */
@Serializable
data class ConfirmExecutionRequestDto(
    val executedDate: String,
    val actualRemovalPercentage: Double,
    val notes: String? = null,
)

/** Answer of the POST and of `GET /thinning-prescriptions/{id}/execution-confirmations` (P63). */
@Serializable
data class ExecutionConfirmationResponseDto(
    val prescriptionId: String,
    val confirmationStatus: String? = null,
    val executedDate: String,
    val actualRemovalPercentage: Double,
    val notes: String? = null,
    val isOpportune: Boolean = true,
    val loadBalance: LoadBalanceDto? = null,
    val caliberProjection: CaliberProjectionDto? = null,
    val yieldEstimate: YieldEstimateDto? = null,
)

@Serializable
data class LoadBalanceDto(
    val preThinningFruitsPerShoot: Double? = null,
    val residualFruitsPerShoot: Double? = null,
    val targetFruitsPerShoot: Double? = null,
    val loadRatio: Double? = null,
    val loadState: String? = null,
)

@Serializable
data class CaliberProjectionDto(
    val status: String? = null,
    val mostLikelySizeGrade: String? = null,
    val sizeGradeLow: String? = null,
    val sizeGradeHigh: String? = null,
    val confidenceLevel: Double? = null,
    val calibrationObservations: Int? = null,
    val requiredObservations: Int? = null,
    val calibratedLoadMin: Double? = null,
    val calibratedLoadMax: Double? = null,
)
