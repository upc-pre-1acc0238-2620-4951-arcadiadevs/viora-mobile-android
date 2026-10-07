package pe.edu.upc.viora.features.phenology.domain.entity

import java.time.Instant
import java.time.LocalDate

/**
 * Domain entity representing winter chilling accumulation (US22) based on Erez dynamic model.
 * Sevillana / Criolla olives require 30 portions between June 1 and August 31.
 */
data class ChillTracker(
    val plotId: String,
    val accumulatedPortions: Double,
    val thresholdPortions: Double,
    val daysAbove24Celsius: Int,
    val seasonState: WinterSeasonState,
    val projectedCompletionDate: LocalDate?,
    val previousWinterCompletionDate: LocalDate?,
    val ensoRisk: EnsoRiskLevel,
    val curvePoints: List<ChillCurvePoint>,
    val syncedAt: Instant,
) {
    /** Portions still required to reach threshold. 0 when completed. */
    val portionsRemaining: Double
        get() = (thresholdPortions - accumulatedPortions).coerceAtLeast(0.0)

    /** Progress ratio between 0.0 and 1.0. */
    val progressFraction: Float
        get() = if (thresholdPortions > 0.0) {
            (accumulatedPortions / thresholdPortions).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    /** True if 30 portions have been accumulated or marked completed. */
    val isCompleted: Boolean
        get() = seasonState == WinterSeasonState.COMPLETED || accumulatedPortions >= thresholdPortions
}

/**
 * 4 operational states for the winter season in coastal Peru (Tacna/Arequipa/Moquegua):
 * - ACCUMULATING: Chilling accumulation ongoing normally.
 * - CHILL_HALTED: High temperatures (>24 °C) or El Niño preventing portion fixation.
 * - COMPLETED: 30 portions reached, floral stimulus assured.
 * - OFF_SEASON: Outside winter season (Sep - May).
 */
enum class WinterSeasonState {
    ACCUMULATING,
    CHILL_HALTED,
    COMPLETED,
    OFF_SEASON,
}

enum class EnsoRiskLevel {
    NEUTRAL,
    ACTIVE,
    HIGH,
}

data class ChillCurvePoint(
    val date: LocalDate,
    val accumulatedThisYear: Double,
    val accumulatedPreviousYear: Double?,
)
