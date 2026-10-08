package pe.edu.upc.viora.features.phenology.infrastructure.mapper

import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.edu.upc.viora.features.phenology.domain.entity.ChillCurvePoint
import pe.edu.upc.viora.features.phenology.domain.entity.ChillProjection
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.PreviousChillSeason
import pe.edu.upc.viora.features.phenology.domain.entity.ThermalAnomaly
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerEntity
import pe.edu.upc.viora.features.phenology.infrastructure.remote.ChillCurvePointDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDto

private val chillJson = Json { ignoreUnknownKeys = true }

/**
 * Maps the `EREZ_CHILLING_PORTIONS` metric to its cache row. Returns null when the response lacks the fields
 * that identify the season, so a malformed answer never replaces good cached data with invented values.
 */
fun MetricDto.toChillEntity(plotId: String, nowEpochMs: Long): ChillTrackerEntity? {
    val details = details ?: return null
    val portions = value ?: return null
    val threshold = details.thresholdTarget ?: return null
    val seasonYear = details.seasonYear ?: return null
    val previous = details.previousSeason

    return ChillTrackerEntity(
        plotId = plotId,
        seasonYear = seasonYear,
        accumulatedPortions = portions,
        thresholdPortions = threshold,
        seasonState = details.seasonState.orEmpty(),
        evaluatedThrough = details.evaluatedThrough,
        completionDate = details.completionDate,
        projectionStatus = details.projectionStatus.orEmpty(),
        projectedCompletionDate = details.projectedCompletionDate,
        daysAbove24Celsius = details.daysAbove24Celsius ?: 0,
        currentWarmStreakDays = details.currentWarmStreakDays ?: 0,
        longestWarmStreakDays = details.longestWarmStreakDays ?: 0,
        thermalAnomaly = details.thermalAnomaly.orEmpty(),
        previousSeasonYear = previous?.seasonYear,
        previousSeasonPortions = previous?.accumulatedPortions,
        previousSeasonCompletionDate = previous?.completionDate,
        curvePointsJson = chillJson.encodeToString(details.dailyCurve.orEmpty()),
        syncedAtEpochMs = nowEpochMs,
    )
}

fun ChillTrackerEntity.toDomain(): ChillTracker {
    val points = runCatching { chillJson.decodeFromString<List<ChillCurvePointDto>>(curvePointsJson) }
        .getOrDefault(emptyList())
        .mapNotNull { dto ->
            dto.date.toLocalDateOrNull()?.let { date ->
                ChillCurvePoint(date, dto.portions, dto.previousSeasonPortions)
            }
        }
    val previous = if (previousSeasonYear != null && previousSeasonPortions != null) {
        PreviousChillSeason(previousSeasonYear, previousSeasonPortions, previousSeasonCompletionDate.toLocalDateOrNull())
    } else {
        null
    }

    return ChillTracker(
        plotId = plotId,
        seasonYear = seasonYear,
        accumulatedPortions = accumulatedPortions,
        thresholdPortions = thresholdPortions,
        seasonState = seasonState.toWinterSeasonState(),
        evaluatedThrough = evaluatedThrough.toLocalDateOrNull(),
        completionDate = completionDate.toLocalDateOrNull(),
        projection = projectionStatus.toChillProjection(),
        projectedCompletionDate = projectedCompletionDate.toLocalDateOrNull(),
        daysAbove24Celsius = daysAbove24Celsius,
        currentWarmStreakDays = currentWarmStreakDays,
        longestWarmStreakDays = longestWarmStreakDays,
        thermalAnomaly = thermalAnomaly.toThermalAnomaly(),
        previousSeason = previous,
        curvePoints = points,
        syncedAt = Instant.ofEpochMilli(syncedAtEpochMs),
    )
}

/** Backend states: IN_PROGRESS, HALTED, COMPLETED, OFF_SEASON. */
fun String.toWinterSeasonState(): WinterSeasonState = when (this) {
    "HALTED" -> WinterSeasonState.CHILL_HALTED
    "COMPLETED" -> WinterSeasonState.COMPLETED
    "OFF_SEASON" -> WinterSeasonState.OFF_SEASON
    else -> WinterSeasonState.ACCUMULATING
}

fun String.toThermalAnomaly(): ThermalAnomaly = when (this) {
    "ACTIVE" -> ThermalAnomaly.ACTIVE
    "RECORDED" -> ThermalAnomaly.RECORDED
    else -> ThermalAnomaly.NONE
}

fun String.toChillProjection(): ChillProjection = when (this) {
    "PROJECTED" -> ChillProjection.PROJECTED
    "NOT_REACHABLE_IN_SEASON" -> ChillProjection.NOT_REACHABLE_IN_SEASON
    "INSUFFICIENT_DATA" -> ChillProjection.INSUFFICIENT_DATA
    else -> ChillProjection.NOT_APPLICABLE
}

private fun String?.toLocalDateOrNull(): LocalDate? = this?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
