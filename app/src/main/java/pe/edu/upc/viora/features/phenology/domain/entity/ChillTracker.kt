package pe.edu.upc.viora.features.phenology.domain.entity

import java.time.Instant
import java.time.LocalDate

/**
 * Winter chill of a plot (US22) computed by the backend with the Dynamic Model (Fishman & Erez) from the hourly
 * temperatures at the plot, plus its warm winter signal (US23). The season runs from June 1 to August 31; outside
 * it the tracker describes the last finished winter.
 */
data class ChillTracker(
    val plotId: String,
    val seasonYear: Int,
    val accumulatedPortions: Double,
    val thresholdPortions: Double,
    val seasonState: WinterSeasonState,
    /** Last day with temperatures, or null when no day of the season has been evaluated yet. */
    val evaluatedThrough: LocalDate?,
    /** First day the threshold was reached, or null. */
    val completionDate: LocalDate?,
    val projection: ChillProjection,
    /** Day the threshold would be reached at the pace of the last 14 days; only when [projection] is PROJECTED. */
    val projectedCompletionDate: LocalDate?,
    /** Days of the season whose maximum was above 24 °C. */
    val daysAbove24Celsius: Int,
    /** Consecutive days above 24 °C up to [evaluatedThrough]. */
    val currentWarmStreakDays: Int,
    /** Longest run of consecutive days above 24 °C in the season. */
    val longestWarmStreakDays: Int,
    val thermalAnomaly: ThermalAnomaly,
    /** The winter before, or null when its temperatures could not be read. */
    val previousSeason: PreviousChillSeason?,
    /** One point per day of the season (92), oldest first. */
    val curvePoints: List<ChillCurvePoint>,
    val syncedAt: Instant,
) {
    /** Portions still required to reach the threshold. 0 when completed. */
    val portionsRemaining: Double
        get() = (thresholdPortions - accumulatedPortions).coerceAtLeast(0.0)

    val isCompleted: Boolean
        get() = completionDate != null
}

/**
 * Operational states of the winter season:
 * - ACCUMULATING: within June 1 - August 31, threshold not reached yet.
 * - CHILL_HALTED: same, but more than 3 days in a row above 24 °C are going on right now.
 * - COMPLETED: within the season, threshold already reached.
 * - OFF_SEASON: outside the season; the data is the last finished winter.
 */
enum class WinterSeasonState {
    ACCUMULATING,
    CHILL_HALTED,
    COMPLETED,
    OFF_SEASON,
}

/**
 * Warm winter signal: a run of more than 3 consecutive days above 24 °C in the season. It is a local thermal
 * rule computed from the plot's temperatures, not the official El Niño index.
 */
enum class ThermalAnomaly {
    NONE,
    ACTIVE,
    RECORDED,
}

/** Whether the backend could project when the threshold will be reached. */
enum class ChillProjection {
    PROJECTED,
    NOT_REACHABLE_IN_SEASON,
    INSUFFICIENT_DATA,
    NOT_APPLICABLE,
}

data class PreviousChillSeason(
    val seasonYear: Int,
    val accumulatedPortions: Double,
    val completionDate: LocalDate?,
)

/**
 * One day of the season. [accumulatedThisYear] is null for the days not evaluated yet and
 * [accumulatedPreviousYear] is null when the previous winter has no data for that day.
 */
data class ChillCurvePoint(
    val date: LocalDate,
    val accumulatedThisYear: Double?,
    val accumulatedPreviousYear: Double?,
)
