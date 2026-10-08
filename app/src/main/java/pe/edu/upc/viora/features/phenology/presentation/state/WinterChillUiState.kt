package pe.edu.upc.viora.features.phenology.presentation.state

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState

sealed interface WinterChillUiState {

    data object Loading : WinterChillUiState

    data class Error(val error: AppError) : WinterChillUiState

    /** The backend has no chill for this plot. */
    data class Empty(val plotName: String, val varietyName: String) : WinterChillUiState

    data class Content(
        val plotName: String,
        val varietyName: String,
        val tracker: ChillTracker,
        val lastSync: Instant?,
        val isRefreshing: Boolean,
        val offline: Boolean,
    ) : WinterChillUiState {

        val seasonState: WinterSeasonState
            get() = tracker.seasonState

        /** Whole portions already fixed (never rounded up, so 29.6 still reads "29 de 30"). */
        val accumulatedPortions: Int
            get() = floor(tracker.accumulatedPortions).toInt()

        val thresholdPortions: Int
            get() = tracker.thresholdPortions.toInt()

        /** Whole portions still missing (rounded up, so it matches [accumulatedPortions]). */
        val portionsRemaining: Int
            get() = ceil(tracker.portionsRemaining).toInt()

        /** Share of the 92 days of the season already evaluated. */
        val seasonProgress: Float
            get() {
                if (seasonState == WinterSeasonState.OFF_SEASON) return 1f
                val start = tracker.curvePoints.firstOrNull()?.date ?: return 0f
                val end = tracker.curvePoints.last().date
                val through = tracker.evaluatedThrough ?: return 0f
                val total = ChronoUnit.DAYS.between(start, end) + 1
                val elapsed = ChronoUnit.DAYS.between(start, through) + 1
                return (elapsed.toFloat() / total).coerceIn(0f, 1f)
            }

        /**
         * Current portions minus the previous winter's on the same day; null when either is missing.
         * Positive means this winter is ahead.
         */
        val differenceWithPreviousWinter: Double?
            get() {
                val through = tracker.evaluatedThrough ?: return null
                val previous = tracker.curvePoints.firstOrNull { it.date == through }?.accumulatedPreviousYear ?: return null
                return tracker.accumulatedPortions - previous
            }

        /**
         * Days this winter completed before the previous one (negative: after); null unless both completed.
         */
        val daysAheadOfPreviousWinter: Long?
            get() {
                val current = tracker.completionDate ?: return null
                val previous = tracker.previousSeason?.completionDate ?: return null
                return ChronoUnit.DAYS.between(current, previous.plusYears(1))
            }
    }
}
