package pe.edu.upc.viora.features.telemetry.presentation.state

import kotlin.math.floor
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.phenology.application.published.ChillTracker
import pe.edu.upc.viora.features.phenology.application.published.WinterSeasonState
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail
import pe.edu.upc.viora.features.telemetry.domain.entity.WeeklyTrendPoint

/** UI state for the deep alert detail screen with weekly trend and mitigation tasks. */
sealed interface AlertDetailUiState {
    data object Loading : AlertDetailUiState

    data class Error(val error: AppError) : AlertDetailUiState

    /**
     * [chill] is the winter chill of the plot, read only for a warm winter alert (T15 · Invierno cálido) to say how
     * far the count got; null for the other types or while it could not be read.
     */
    data class Content(
        val detail: IncidentDetail,
        val isSnoozing: Boolean = false,
        val userFeedbackMessage: String? = null,
        val chill: ChillTracker? = null,
    ) : AlertDetailUiState {

        /** "4 días seguidos": the warm days in a row up to the last day of the trend. */
        val warmStreakDays: Int
            get() = warmStreakDays(detail.weeklyTrend, detail.thresholdValue)

        /** The first line of "Qué cambia" of a warm winter: what the warm spell did to the chill count. */
        val chillFact: ChillFact
            get() {
                val tracker = chill ?: return ChillFact.Unknown(warmStreakDays)
                val portions = floor(tracker.accumulatedPortions).toInt()
                val threshold = floor(tracker.thresholdPortions).toInt()
                // "Sin sumar porciones" only when the backend says the count is halted, as P80 does.
                return if (tracker.seasonState == WinterSeasonState.CHILL_HALTED) {
                    ChillFact.Halted(tracker.currentWarmStreakDays.coerceAtLeast(warmStreakDays), portions, threshold)
                } else {
                    ChillFact.AtRisk(warmStreakDays.takeIf { it > 0 } ?: tracker.currentWarmStreakDays, portions, threshold)
                }
            }
    }
}

/** What a warm spell did to the chill count, for the first line of T15 "Qué cambia". */
sealed interface ChillFact {
    /** "Frío frenado: 4 días sin sumar porciones (21 de 30)." */
    data class Halted(val days: Int, val portions: Int, val threshold: Int) : ChillFact

    /** The count still runs: "Frío en riesgo: 4 días seguidos sobre 24 °C (21 de 30 porciones)." */
    data class AtRisk(val days: Int, val portions: Int, val threshold: Int) : ChillFact

    /** Without the chill of the plot only the warm days are known. */
    data class Unknown(val days: Int) : ChillFact
}

/** Consecutive days above [threshold], counted back from the newest point of [trend]. */
internal fun warmStreakDays(trend: List<WeeklyTrendPoint>, threshold: Double): Int =
    trend.sortedBy { it.timestamp }.takeLastWhile { it.value > threshold }.size
