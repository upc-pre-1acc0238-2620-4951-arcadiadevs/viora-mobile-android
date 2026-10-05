package pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.StabilizationCurve
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.StabilizationStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.ThinningBalance
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.ThinningStatus
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.StabilizationDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.ThinningBalanceDto

fun HarvestSettlementDto.toEntity(): HarvestSettlementEntity {
    val thinning = thinningBalance ?: ThinningBalanceDto()
    val curve = stabilization ?: StabilizationDto()
    return HarvestSettlementEntity(
        id = id,
        reportId = reportId,
        plotId = plotId,
        campaignYear = campaignYear,
        greenKg = greenOlivesKg,
        blackKg = blackOlivesKg,
        totalYieldKg = totalYieldKg,
        commercialFruitsPerKg = commercialFruitsPerKg,
        notes = notes,
        status = status.orEmpty(),
        settledAt = settledAt,
        thinningStatus = thinning.status.orEmpty(),
        thinningExecutedDate = thinning.executedDate,
        thinningPrescribedPct = thinning.prescribedRemovalPercentage,
        thinningActualPct = thinning.actualRemovalPercentage,
        thinningDeviationPp = thinning.deviationPercentagePoints,
        stabilizationStatus = curve.status.orEmpty(),
        baselineCampaigns = curve.baselineCampaigns,
        settledCampaigns = curve.settledCampaigns,
        baselineYieldKg = curve.baselineYieldKg,
        baselineAlternationIndex = curve.baselineAlternationIndex,
        managedAlternationIndex = curve.managedAlternationIndex,
        amplitudeReductionRate = curve.amplitudeReductionRate,
        targetAchieved = curve.targetAchieved,
        interannualVarianceKg2 = curve.interannualVarianceKg2,
        coefficientOfVariation = curve.coefficientOfVariation,
        requiredConsecutivePairs = curve.requiredConsecutivePairs,
    )
}

fun HarvestSettlementEntity.toDomain(): HarvestSettlement = HarvestSettlement(
    id = id,
    reportId = reportId,
    plotId = plotId,
    campaignYear = campaignYear,
    greenKg = greenKg,
    blackKg = blackKg,
    totalYieldKg = totalYieldKg,
    commercialFruitsPerKg = commercialFruitsPerKg,
    notes = notes,
    status = status.toSettlementStatus(),
    settledAt = settledAt.toInstantOrNull() ?: Instant.EPOCH,
    thinningBalance = ThinningBalance(
        status = thinningStatus.toThinningStatus(),
        executedDate = thinningExecutedDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        prescribedRemovalPercentage = thinningPrescribedPct,
        actualRemovalPercentage = thinningActualPct,
        deviationPercentagePoints = thinningDeviationPp,
    ),
    stabilization = StabilizationCurve(
        status = stabilizationStatus.toStabilizationStatus(),
        baselineCampaigns = baselineCampaigns,
        settledCampaigns = settledCampaigns,
        baselineYieldKg = baselineYieldKg,
        baselineAlternationIndex = baselineAlternationIndex,
        managedAlternationIndex = managedAlternationIndex,
        amplitudeReductionRate = amplitudeReductionRate,
        targetAchieved = targetAchieved,
        interannualVarianceKg2 = interannualVarianceKg2,
        coefficientOfVariation = coefficientOfVariation,
        requiredConsecutivePairs = requiredConsecutivePairs,
    ),
)

/** Unrecognized or missing values fall back to SETTLED, the plain state of a settlement. */
fun String.toSettlementStatus(): SettlementStatus =
    if (this == "AUDITED") SettlementStatus.AUDITED else SettlementStatus.SETTLED

/** Unrecognized or missing values fall back to NOT_RECORDED: no claim about the thinning is made. */
fun String.toThinningStatus(): ThinningStatus = when (this) {
    "EXECUTED_ON_TIME" -> ThinningStatus.EXECUTED_ON_TIME
    "EXECUTED_LATE" -> ThinningStatus.EXECUTED_LATE
    else -> ThinningStatus.NOT_RECORDED
}

/** Unrecognized or missing values fall back to INSUFFICIENT_SETTLEMENTS: no curve is claimed. */
fun String.toStabilizationStatus(): StabilizationStatus = when (this) {
    "EVALUATED" -> StabilizationStatus.EVALUATED
    "INSUFFICIENT_BASELINE" -> StabilizationStatus.INSUFFICIENT_BASELINE
    "NO_BASELINE_ALTERNATION" -> StabilizationStatus.NO_BASELINE_ALTERNATION
    else -> StabilizationStatus.INSUFFICIENT_SETTLEMENTS
}

private fun String?.toInstantOrNull(): Instant? = this?.let { raw ->
    runCatching { Instant.parse(raw) }
        .recoverCatching { OffsetDateTime.parse(raw).toInstant() }
        .getOrNull()
}
