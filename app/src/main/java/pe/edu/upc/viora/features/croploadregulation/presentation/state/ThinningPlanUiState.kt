package pe.edu.upc.viora.features.croploadregulation.presentation.state

import java.time.Instant
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

/** P61 · Plan del lote. */
sealed interface ThinningPlanUiState {

    data object Loading : ThinningPlanUiState

    data class Error(val error: AppError) : ThinningPlanUiState

    data class Content(
        val plotName: String,
        val variety: OliveVariety?,
        val areaHectares: Double?,
        val campaignYear: Int,
        /** Null when nothing was sampled for the campaign yet. */
        val prescription: ThinningPrescription?,
        /** The thinning recorded against the plan, when it is on the phone: P61 shows what was really removed. */
        val execution: ThinningExecution? = null,
        val today: LocalDate,
        val syncedAt: Instant,
        val isRefreshing: Boolean,
        /** The last refresh failed without signal: the screen shows the saved plan and when it was computed. */
        val offline: Boolean,
    ) : ThinningPlanUiState {

        val stage: PlanStage
            get() {
                val plan = prescription ?: return PlanStage.SAMPLING
                return when {
                    plan.isIssued -> PlanStage.PRESCRIBED
                    PrescriptionBlocker.SAMPLING_NOT_REPRESENTATIVE in plan.blockers -> PlanStage.SAMPLING
                    PrescriptionBlocker.TARGET_NOT_CONFIGURED in plan.blockers -> PlanStage.NO_PROFILE
                    PrescriptionBlocker.FULL_BLOOM_MISSING in plan.blockers -> PlanStage.NO_BLOOM
                    else -> PlanStage.SAMPLING
                }
            }

        val sampledTrees: Int
            get() = (prescription?.sampledTrees ?: 0).coerceIn(0, ThinningPrescription.SAMPLE_SIZE)

        val treesNeeded: Int
            get() = prescription?.treesNeeded ?: ThinningPrescription.SAMPLE_SIZE
    }
}

/** What the plan can show: each stage is one P61 variant. */
enum class PlanStage {
    /** "Faltan N árboles": the load cannot be computed yet. */
    SAMPLING,

    /** "Sin perfil técnico": no approved target for the variety. */
    NO_PROFILE,

    /** "Falta la floración": the window needs the full bloom date (P64). */
    NO_BLOOM,

    /** The prescription is issued. */
    PRESCRIBED,
}

/** The P64 sheet: the full bloom date, moved a day at a time, never in the future nor before the campaign. */
data class FullBloomSheetState(
    val date: LocalDate,
    val today: LocalDate,
    val earliest: LocalDate,
    val isSaving: Boolean = false,
    val error: AppError? = null,
) {
    val canStepBack: Boolean
        get() = date.isAfter(earliest)

    val canStepForward: Boolean
        get() = date.isBefore(today)
}

/**
 * "3 de cada 10": the share to remove as the smallest whole fraction out of at most ten fruits that matches it best,
 * e.g. 30 % = 3 of 10, 17 % = 1 of 6. Null below half a fruit in ten.
 */
fun removalFraction(percentage: Double): Pair<Int, Int>? {
    val share = percentage / 100.0
    if (share <= 0.0) return null
    return (2..10)
        .map { of -> (share * of).roundToInt() to of }
        .filter { (count, of) -> count in 1 until of }
        .minWithOrNull(compareBy<Pair<Int, Int>> { (count, of) -> abs(count.toDouble() / of - share) }.thenBy { it.second })
}

/** How much more fruit than the target, in whole percent: a load ratio of 1.43 is 43. */
fun excessPercent(loadRatio: Double): Int = ((loadRatio - 1.0) * 100).roundToInt().coerceAtLeast(0)
