package pe.edu.upc.viora.features.croploadregulation.presentation.state

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningTimeliness
import pe.edu.upc.viora.features.croploadregulation.domain.entity.thinningTimeliness
import pe.edu.upc.viora.core.domain.AppError

/**
 * P62 · Registrar aclareo: the day of the thinning, the share of fruit removed (preloaded with the prescription) and an
 * optional note. [earliest] is the day the window opened: the backend refuses a thinning before it.
 */
data class RegisterThinningUiState(
    val plotName: String,
    val campaignYear: Int,
    val isLoading: Boolean = true,
    /** Null while the plan is not downloaded or has no prescription: there is nothing to record against. */
    val prescriptionId: String? = null,
    val prescribedPercentage: Int? = null,
    val windowOpensOn: LocalDate? = null,
    val windowClosesOn: LocalDate? = null,
    val executedOn: LocalDate,
    val earliest: LocalDate,
    val today: LocalDate,
    val percentage: Int = 0,
    val note: String = "",
    val isSaving: Boolean = false,
    val error: AppError? = null,
) {
    /** Something the producer typed that leaving would throw away. */
    val hasChanges: Boolean
        get() = note.isNotBlank() || (prescribedPercentage != null && percentage != prescribedPercentage)

    val windowCheck: WindowCheck?
        get() {
            val opens = windowOpensOn ?: return null
            val closes = windowClosesOn ?: return null
            return when {
                thinningTimeliness(today, opens, closes) == ThinningTimeliness.BEFORE -> WindowCheck.NotOpenYet(opens)
                thinningTimeliness(executedOn, opens, closes) == ThinningTimeliness.LATE -> WindowCheck.Late(closes)
                else -> WindowCheck.Within(ChronoUnit.DAYS.between(today, closes).toInt().takeIf { it >= 0 })
            }
        }

    val canSave: Boolean
        get() = prescriptionId != null && !isSaving && !isLoading && windowCheck !is WindowCheck.NotOpenYet &&
            !executedOn.isBefore(earliest) && !executedOn.isAfter(today) && note.length <= MAX_NOTE_LENGTH

    companion object {
        const val PERCENT_STEP = 5
        const val MAX_NOTE_LENGTH = 2000
    }
}

/** Where the chosen day falls against the intervention window (P62 chip or warning). */
sealed interface WindowCheck {
    /** In the window; [daysLeft] counts from today, null once the window already closed. */
    data class Within(val daysLeft: Int?) : WindowCheck

    /** After the pit hardened: it is recorded, marked late. */
    data class Late(val closedOn: LocalDate) : WindowCheck

    /** The window has not opened: nothing can be recorded yet. */
    data class NotOpenYet(val opensOn: LocalDate) : WindowCheck
}
