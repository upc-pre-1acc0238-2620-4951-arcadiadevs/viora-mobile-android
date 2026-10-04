package pe.edu.upc.viora.features.phenology.infrastructure.mapper

import java.time.Instant
import java.time.OffsetDateTime
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.infrastructure.local.BearingIndexEntity
import pe.edu.upc.viora.features.phenology.infrastructure.local.HarvestRecordEntity
import pe.edu.upc.viora.features.phenology.infrastructure.remote.HarvestRecordDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.RectifyHarvestYieldRequestDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.RecordHarvestYieldRequestDto

fun HarvestRecordDto.toEntity(): HarvestRecordEntity = HarvestRecordEntity(
    id = id,
    plotId = plotId,
    campaignYear = campaignYear,
    totalYieldKg = totalYieldKg,
    greenKg = greenKg,
    blackKg = blackKg,
    bearing = bearingClassification.orEmpty(),
    recordedAt = recordedAt,
)

fun HarvestRecordEntity.toDomain(): HarvestRecord = HarvestRecord(
    id = id,
    plotId = plotId,
    campaignYear = campaignYear,
    totalYieldKg = totalYieldKg,
    greenKg = greenKg,
    blackKg = blackKg,
    bearing = bearing.toBearingYear(),
    recordedAt = recordedAt.toInstantOrNull() ?: Instant.EPOCH,
)

/** Maps the backend classification; anything unrecognized (incl. INSUFFICIENT_DATA) is UNKNOWN. */
fun String.toBearingYear(): BearingYear = when (this) {
    "ON_YEAR" -> BearingYear.ON
    "OFF_YEAR" -> BearingYear.OFF
    "BALANCED" -> BearingYear.BALANCED
    else -> BearingYear.UNKNOWN
}

fun MetricDto.toEntity(plotId: String): BearingIndexEntity = BearingIndexEntity(
    plotId = plotId,
    value = value,
    evaluatedYears = details?.evaluatedYearsCount ?: 0,
    evaluatedAt = evaluatedAt,
)

fun BearingIndexEntity.toDomain(): BearingIndex = BearingIndex(
    value = value,
    evaluatedYears = evaluatedYears,
    evaluatedAt = evaluatedAt.toInstantOrNull(),
)

fun recordRequestDto(campaignYear: Int, totalYieldKg: Double): RecordHarvestYieldRequestDto =
    RecordHarvestYieldRequestDto(campaignYear = campaignYear, totalYieldKg = totalYieldKg)

fun rectifyRequestDto(totalYieldKg: Double): RectifyHarvestYieldRequestDto =
    RectifyHarvestYieldRequestDto(totalYieldKg = totalYieldKg)

private fun String?.toInstantOrNull(): Instant? = this?.let { raw ->
    runCatching { Instant.parse(raw) }
        .recoverCatching { OffsetDateTime.parse(raw).toInstant() }
        .getOrNull()
}
