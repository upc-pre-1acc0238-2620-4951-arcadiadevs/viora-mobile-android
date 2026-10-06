package pe.edu.upc.viora.features.harvestsettlement.domain.entity

import java.time.Instant
import java.time.LocalDate

/**
 * A campaign the producer already settled ("cosecha asentada", US29). [greenKg] and [blackKg]
 * are the split by fruit maturity; [settledAt] is when the server stored the settlement.
 * [receiptNumber], [weighedOn], [millTicketNumber] and [commercialSizeGrade] are nullable because
 * older backend versions do not send them.
 */
data class HarvestSettlement(
    val id: String,
    val reportId: String,
    val plotId: String,
    val campaignYear: Int,
    val greenKg: Double,
    val blackKg: Double,
    val totalYieldKg: Double,
    val commercialFruitsPerKg: Double?,
    val notes: String?,
    val receiptNumber: String? = null,
    val weighedOn: LocalDate? = null,
    val millTicketNumber: String? = null,
    val commercialSizeGrade: String? = null,
    val status: SettlementStatus,
    val settledAt: Instant,
    val thinningBalance: ThinningBalance,
    val stabilization: StabilizationCurve,
)

enum class SettlementStatus { SETTLED, AUDITED }

/** How the campaign's thinning compared with the prescription. */
data class ThinningBalance(
    val status: ThinningStatus,
    val executedDate: LocalDate?,
    val prescribedRemovalPercentage: Double?,
    val actualRemovalPercentage: Double?,
    val deviationPercentagePoints: Double?,
)

enum class ThinningStatus { EXECUTED_ON_TIME, EXECUTED_LATE, NOT_RECORDED }

/** Whether the managed alternation of the plot is stabilizing, evaluated over its settlements. */
data class StabilizationCurve(
    val status: StabilizationStatus,
    val baselineCampaigns: Int,
    val settledCampaigns: Int,
    val baselineYieldKg: Double?,
    val baselineAlternationIndex: Double?,
    val managedAlternationIndex: Double?,
    val amplitudeReductionRate: Double?,
    val targetAchieved: Boolean?,
    val interannualVarianceKg2: Double?,
    val coefficientOfVariation: Double?,
    val requiredConsecutivePairs: Int,
)

enum class StabilizationStatus {
    EVALUATED,
    INSUFFICIENT_BASELINE,
    INSUFFICIENT_SETTLEMENTS,
    NO_BASELINE_ALTERNATION,
}
