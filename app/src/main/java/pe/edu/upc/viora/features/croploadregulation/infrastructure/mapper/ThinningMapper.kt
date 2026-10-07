package pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper

import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.math.PI
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingDetailedReport
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningEventType
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.Timeliness
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.PlotSamplingStateResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.SamplingDetailedResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.SamplingSummaryResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.SamplingTreeItemDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ThinningEventItemDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.TreeSampleRequestDto

fun TreeSample.toRequestDto(): TreeSampleRequestDto {
    val diameterMm = when {
        trunkDiameterMm != null && trunkDiameterMm > 0 -> trunkDiameterMm
        trunkCircumferenceCm != null && trunkCircumferenceCm > 0 -> (trunkCircumferenceCm * 10.0) / PI
        else -> 100.0
    }
    return TreeSampleRequestDto(
        treeTag = treeIdentifier,
        shootCount = shootsCount,
        fruitSetCount = fruitSetCount,
        trunkDiameterMm = Math.round(diameterMm * 10.0) / 10.0,
        samplingDate = observedOn.toString(),
    )
}

fun SamplingSummaryResponseDto.toDomain(): SamplingSummary = SamplingSummary(
    plotId = plotId,
    campaignYear = campaignYear,
    evaluatedTreesCount = sampledTreesCount,
    sampledShootsCount = sampledShootsCount,
    sampledFruitSetCount = sampledFruitSetCount,
    meanFruitsPerShoot = meanFruitsPerShoot,
    isRepresentative = isRepresentative,
    treesNeeded = treesNeeded,
    loadUnit = loadUnit,
)

fun PlotSamplingStateResponseDto.toDomain(): PlotSamplingOverview = PlotSamplingOverview(
    plotId = plotId,
    plotName = plotName,
    variety = variety.lowercase().replaceFirstChar { it.uppercase() },
    areaHectares = areaHectares,
    campaignYear = campaignYear,
    samplingStatus = when (samplingStatus) {
        "IN_PROGRESS" -> SamplingStatus.IN_PROGRESS
        "COMPLETED" -> SamplingStatus.COMPLETED
        else -> SamplingStatus.NOT_STARTED
    },
    sampledTreesCount = sampledTreesCount,
    treesNeeded = treesNeeded,
    isRepresentative = isRepresentative,
)


fun ThinningEventItemDto.toDomain(): ThinningEvent = ThinningEvent(
    id = id,
    eventType = when (eventType) {
        "THINNING_EXECUTED" -> ThinningEventType.THINNING_EXECUTED
        else -> ThinningEventType.SAMPLING_COMPLETED
    },
    prescriptionId = prescriptionId,
    confirmationId = confirmationId,
    plotId = plotId,
    plotName = plotName,
    campaignYear = campaignYear,
    occurredAt = parseInstantOrNow(occurredAt),
    evaluatedTreesCount = evaluatedTreesCount,
    totalShootsCount = totalShootsCount,
    totalFruitsCount = totalFruitsCount,
    meanFruitsPerShoot = meanFruitsPerShoot,
    isRepresentative = isRepresentative,
    removalPercentage = removalPercentage,
    removedKg = removedKg,
    executedDate = executedDate?.let { parseLocalDateOrNull(it) },
    laborCrewSize = laborCrewSize,
    timeliness = when (timeliness) {
        "OPTIMAL" -> Timeliness.OPTIMAL
        "LATE" -> Timeliness.LATE
        else -> null
    },
)

fun SamplingDetailedResponseDto.toDomain(): SamplingDetailedReport = SamplingDetailedReport(
    summary = SamplingSummary(
        plotId = plotId,
        campaignYear = campaignYear,
        evaluatedTreesCount = sampledTreesCount,
        sampledShootsCount = sampledShootsCount,
        sampledFruitSetCount = sampledFruitSetCount,
        meanFruitsPerShoot = meanFruitsPerShoot,
        isRepresentative = isRepresentative,
        treesNeeded = treesNeeded,
        loadUnit = loadUnit,
    ),
    trees = trees.map { it.toDomain() },
)

fun SamplingTreeItemDto.toDomain(): TreeSample = TreeSample(
    treeIdentifier = treeTag,
    shootsCount = shootCount,
    fruitSetCount = fruitSetCount,
    trunkDiameterMm = trunkDiameterMm,
    trunkCircumferenceCm = trunkDiameterMm?.let { (it * PI) / 10.0 },
    observedOn = parseLocalDateOrNull(samplingDate) ?: LocalDate.now(),
    isSynced = true,
)

private fun parseInstantOrNow(iso: String): Instant = try {
    Instant.parse(iso)
} catch (_: DateTimeParseException) {
    Instant.now()
}

private fun parseLocalDateOrNull(iso: String): LocalDate? = try {
    when {
        iso.isBlank() -> null
        iso.contains("T") -> LocalDate.parse(iso.substringBefore("T"))
        else -> LocalDate.parse(iso)
    }
} catch (_: DateTimeParseException) {
    null
}
