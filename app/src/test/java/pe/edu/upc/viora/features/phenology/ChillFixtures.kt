package pe.edu.upc.viora.features.phenology

import java.time.Instant
import java.time.LocalDate
import pe.edu.upc.viora.features.phenology.domain.entity.ChillCurvePoint
import pe.edu.upc.viora.features.phenology.domain.entity.ChillProjection
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.PreviousChillSeason
import pe.edu.upc.viora.features.phenology.domain.entity.ThermalAnomaly
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerEntity

/**
 * The real answer of `GET /plots/{id}/metrics?name=CHILLING` for a plot in Tacna on 2026-10-07 (viora-platform,
 * branch feature/phenology-real-chill-accumulation), with the 92-day curve cut to three days.
 */
const val REAL_CHILL_METRIC_JSON = """
[{"metricName":"EREZ_CHILLING_PORTIONS","value":3.03,"qualitativeCategory":"DEFICIENT",
"details":{"model":"Dynamic Model (Fishman & Erez)","weatherSource":"Open-Meteo historical weather at the plot centroid",
"thresholdTarget":30.0,"completionPercentage":10.1,"seasonYear":2026,"seasonStart":"2026-06-01","seasonEnd":"2026-08-31",
"evaluatedThrough":"2026-08-31","completionDate":null,"idleDays":12,"seasonState":"OFF_SEASON","daysAbove24Celsius":1,
"currentWarmStreakDays":0,"longestWarmStreakDays":1,"thermalAnomaly":"NONE","projectionStatus":"NOT_APPLICABLE",
"projectedCompletionDate":null,
"previousSeason":{"seasonYear":2025,"accumulatedPortions":27.21,"completionDate":null,"daysAbove24Celsius":1},
"dailyCurve":[{"date":"2026-06-01","portions":0.0,"previousSeasonPortions":0.0},
{"date":"2026-07-16","portions":2.02,"previousSeasonPortions":10.1},
{"date":"2026-08-31","portions":3.03,"previousSeasonPortions":27.21}]},
"evaluatedAt":"2026-10-08T04:52:41Z"}]
"""

fun chillTracker(
    accumulated: Double = 24.43,
    threshold: Double = 30.0,
    seasonState: WinterSeasonState = WinterSeasonState.ACCUMULATING,
    evaluatedThrough: LocalDate? = LocalDate.of(2026, 6, 30),
    completionDate: LocalDate? = null,
    projection: ChillProjection = ChillProjection.PROJECTED,
    projectedCompletionDate: LocalDate? = LocalDate.of(2026, 7, 7),
    thermalAnomaly: ThermalAnomaly = ThermalAnomaly.NONE,
    previousSeason: PreviousChillSeason? = PreviousChillSeason(2025, 27.21, null),
    curvePoints: List<ChillCurvePoint> = emptyList(),
) = ChillTracker(
    plotId = "plot-1",
    seasonYear = 2026,
    accumulatedPortions = accumulated,
    thresholdPortions = threshold,
    seasonState = seasonState,
    evaluatedThrough = evaluatedThrough,
    completionDate = completionDate,
    projection = projection,
    projectedCompletionDate = projectedCompletionDate,
    daysAbove24Celsius = 0,
    currentWarmStreakDays = 0,
    longestWarmStreakDays = 0,
    thermalAnomaly = thermalAnomaly,
    previousSeason = previousSeason,
    curvePoints = curvePoints,
    syncedAt = Instant.parse("2026-07-01T12:00:00Z"),
)

fun chillEntity(plotId: String = "plot-1", accumulated: Double = 18.0) = ChillTrackerEntity(
    plotId = plotId,
    seasonYear = 2026,
    accumulatedPortions = accumulated,
    thresholdPortions = 30.0,
    seasonState = "IN_PROGRESS",
    evaluatedThrough = "2026-06-30",
    completionDate = null,
    projectionStatus = "PROJECTED",
    projectedCompletionDate = "2026-07-20",
    daysAbove24Celsius = 0,
    currentWarmStreakDays = 0,
    longestWarmStreakDays = 0,
    thermalAnomaly = "NONE",
    previousSeasonYear = 2025,
    previousSeasonPortions = 27.21,
    previousSeasonCompletionDate = null,
    curvePointsJson = "[]",
    syncedAtEpochMs = 1000L,
)
