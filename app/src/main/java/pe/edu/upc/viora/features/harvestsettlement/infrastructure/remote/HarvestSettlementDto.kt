package pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote

import kotlinx.serialization.Serializable

/** DTO for `/api/v1/plots/{plotId}/harvest-settlements`. */
@Serializable
data class HarvestSettlementDto(
    val id: String,
    val reportId: String,
    val plotId: String,
    val campaignYear: Int,
    val greenOlivesKg: Double,
    val blackOlivesKg: Double,
    val totalYieldKg: Double,
    val commercialFruitsPerKg: Double? = null,
    val notes: String? = null,
    val receiptNumber: String? = null,
    val weighedOn: String? = null,
    val millTicketNumber: String? = null,
    val commercialSizeGrade: String? = null,
    val status: String? = null,
    val settledAt: String? = null,
    val thinningBalance: ThinningBalanceDto? = null,
    val stabilization: StabilizationDto? = null,
)

@Serializable
data class ThinningBalanceDto(
    val status: String? = null,
    val executedDate: String? = null,
    val prescribedRemovalPercentage: Double? = null,
    val actualRemovalPercentage: Double? = null,
    val deviationPercentagePoints: Double? = null,
)

@Serializable
data class StabilizationDto(
    val status: String? = null,
    val baselineCampaigns: Int = 0,
    val settledCampaigns: Int = 0,
    val baselineYieldKg: Double? = null,
    val baselineAlternationIndex: Double? = null,
    val managedAlternationIndex: Double? = null,
    val amplitudeReductionRate: Double? = null,
    val targetAchieved: Boolean? = null,
    val interannualVarianceKg2: Double? = null,
    val coefficientOfVariation: Double? = null,
    val requiredConsecutivePairs: Int = 0,
)

/** Body of `POST /plots/{plotId}/harvest-settlements`; [weighedOn] is an ISO `yyyy-MM-dd` date. */
@Serializable
data class SettleHarvestRequestDto(
    val campaignYear: Int,
    val greenOlivesKg: Double,
    val blackOlivesKg: Double,
    val weighedOn: String,
    val millTicketNumber: String? = null,
    val commercialFruitsPerKg: Double? = null,
    val notes: String? = null,
)

/** The `existingSettlement` ProblemDetail property of a 409 `HARVESTSETTLEMENT_CONFLICT`. */
@Serializable
data class ExistingSettlementDto(
    val campaignYear: Int,
    val totalYieldKg: Double,
    val receiptNumber: String? = null,
    val weighedOn: String? = null,
)
