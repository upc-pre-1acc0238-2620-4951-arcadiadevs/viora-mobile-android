package pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper

import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.math.PI
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningEventType
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.Timeliness
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.PlotSamplingStateResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.SamplingSummaryResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ThinningEventItemDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.TreeSampleRequestDto

fun TreeSample.toRequestDto(): TreeSampleRequestDto {
    val diameterMm = when {
        trunkDiameterMm != null -> trunkDiameterMm
        trunkCircumferenceCm != null && trunkCircumferenceCm > 0 -> (trunkCircumferenceCm * 10.0) / PI
        else -> null
    }
    return TreeSampleRequestDto(
        treeIdentifier = treeIdentifier,
        shootsCount = shootsCount,
        fruitSetCount = fruitSetCount,
        trunkDiameterMm = diameterMm?.let { Math.round(it * 10.0) / 10.0 },
        observedOn = observedOn.toString(),
    )
}

fun SamplingSummaryResponseDto.toDomain(): SamplingSummary = SamplingSummary(
    prescriptionId = prescriptionId,
    plotId = plotId,
    campaignYear = campaignYear,
    status = status,
    evaluatedTreesCount = evaluatedTreesCount,
    targetTreesCount = targetTreesCount,
    coveragePercentage = coveragePercentage,
    meanFruitsPerShoot = meanFruitsPerShoot,
    sampledFruitSetCount = sampledFruitSetCount,
    isRepresentative = isRepresentative,
    updatedAt = parseInstantOrNow(updatedAt),
)

fun PlotSamplingStateResponseDto.toDomain(): PlotSamplingOverview = PlotSamplingOverview(
    plotId = plotId,
    plotName = plotName,
    variety = variety,
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
    executionId = executionId,
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
    executionDate = executionDate?.let { parseLocalDateOrNull(it) },
    laborCrewSize = laborCrewSize,
    timeliness = when (timeliness) {
        "OPTIMAL" -> Timeliness.OPTIMAL
        "LATE" -> Timeliness.LATE
        else -> null
    },
)

private fun parseInstantOrNow(iso: String): Instant = try {
    Instant.parse(iso)
} catch (_: DateTimeParseException) {
    Instant.now()
}

private fun parseLocalDateOrNull(iso: String): LocalDate? = try {
    LocalDate.parse(iso)
} catch (_: DateTimeParseException) {
    null
}
