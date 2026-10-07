package pe.edu.upc.viora.features.phenology.infrastructure.mapper

import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.edu.upc.viora.features.phenology.domain.entity.ChillCurvePoint
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerEntity
import pe.edu.upc.viora.features.phenology.infrastructure.remote.ChillCurvePointDto
import pe.edu.upc.viora.features.phenology.infrastructure.remote.MetricDto

private val chillJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}

private const val DEFAULT_CHILL_THRESHOLD = 30.0

fun MetricDto.toChillEntity(plotId: String, nowEpochMs: Long): ChillTrackerEntity {
    val accumulated = details?.portionsAccumulated ?: value ?: 0.0
    val threshold = details?.thresholdPortions ?: DEFAULT_CHILL_THRESHOLD
    val days = details?.daysAbove24Celsius ?: details?.daysAbove24C ?: 0
    val state = details?.seasonState ?: qualitativeCategory ?: "ACCUMULATING"
    val risk = details?.ensoRisk ?: "NEUTRAL"
    val points = details?.curvePoints.orEmpty()
    val serializedPoints = runCatching { chillJson.encodeToString(points) }.getOrDefault("[]")

    return ChillTrackerEntity(
        plotId = plotId,
        accumulatedPortions = accumulated,
        thresholdPortions = threshold,
        daysAbove24Celsius = days,
        seasonState = state,
        projectedCompletionDate = details?.projectedCompletionDate,
        previousWinterCompletionDate = details?.previousWinterCompletionDate,
        ensoRisk = risk,
        curvePointsJson = serializedPoints,
        syncedAtEpochMs = nowEpochMs,
    )
}

fun ChillTrackerEntity.toDomain(): ChillTracker {
    val points: List<ChillCurvePoint> = runCatching {
        chillJson.decodeFromString<List<ChillCurvePointDto>>(curvePointsJson).map { dto ->
            ChillCurvePoint(
                date = runCatching { LocalDate.parse(dto.date) }.getOrDefault(LocalDate.now()),
                accumulatedThisYear = dto.accumulatedThisYear,
                accumulatedPreviousYear = dto.accumulatedPreviousYear,
            )
        }
    }.getOrDefault(emptyList())

    return ChillTracker(
        plotId = plotId,
        accumulatedPortions = accumulatedPortions,
        thresholdPortions = thresholdPortions,
        daysAbove24Celsius = daysAbove24Celsius,
        seasonState = seasonState.toWinterSeasonState(),
        projectedCompletionDate = projectedCompletionDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        previousWinterCompletionDate = previousWinterCompletionDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        ensoRisk = ensoRisk.toEnsoRiskLevel(),
        curvePoints = points,
        syncedAt = Instant.ofEpochMilli(syncedAtEpochMs),
    )
}

fun String.toWinterSeasonState(): WinterSeasonState = when (uppercase()) {
    "ACCUMULATING" -> WinterSeasonState.ACCUMULATING
    "CHILL_HALTED", "HALTED", "FRENADO" -> WinterSeasonState.CHILL_HALTED
    "COMPLETED", "ESTIMULO_COMPLETADO" -> WinterSeasonState.COMPLETED
    "OFF_SEASON", "FUERA_DE_TEMPORADA" -> WinterSeasonState.OFF_SEASON
    else -> WinterSeasonState.ACCUMULATING
}

fun String.toEnsoRiskLevel(): EnsoRiskLevel = when (uppercase()) {
    "ACTIVE", "ACTIVO" -> EnsoRiskLevel.ACTIVE
    "HIGH", "ALTO" -> EnsoRiskLevel.HIGH
    else -> EnsoRiskLevel.NEUTRAL
}
