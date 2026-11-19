package pe.edu.upc.viora.features.phenology.domain

import java.time.LocalDate
import java.time.MonthDay
import pe.edu.upc.viora.core.domain.OliveVariety

/**
 * Where the start date of a phase comes from, so the producer can tell an estimate from a date that is theirs:
 * [USUAL] is the usual date of the variety, [ADJUSTED] the one the producer corrected for the plot, and [RECORDED]
 * the one the plot's own records fix (the full bloom they recorded, the thinning window of their plan).
 */
enum class PhaseDateSource { USUAL, ADJUSTED, RECORDED }

/**
 * The start dates of phases that the producer corrected for one plot. They are month and day, so the correction
 * holds every campaign: a producer whose bloom starts in July does not have to say so each year.
 */
data class PhaseAdjustments(val starts: Map<OlivePhase, MonthDay> = emptyMap()) {

    operator fun get(phase: OlivePhase): MonthDay? = starts[phase]

    fun adjusted(phase: OlivePhase, on: MonthDay): PhaseAdjustments = copy(starts = starts + (phase to on))

    fun reset(phase: OlivePhase): PhaseAdjustments = copy(starts = starts - phase)

    val isEmpty: Boolean get() = starts.isEmpty()

    companion object {
        val NONE = PhaseAdjustments()
    }
}

/** The earliest and the latest day a phase can start on, so that the phases keep their order. */
data class DateBounds(val earliest: LocalDate, val latest: LocalDate) {
    operator fun contains(date: LocalDate): Boolean = !date.isBefore(earliest) && !date.isAfter(latest)
}

/** The usual dates of the olive year of one variety, before the producer or the plot's records move them. */
data class PhaseProfile(
    val dormancyStarts: MonthDay,
    val budbreak: MonthDay,
    val fullBloom: MonthDay,
    val thinningOpens: MonthDay,
    val thinningCloses: MonthDay,
    val harvestStarts: MonthDay,
    val harvestEnds: MonthDay,
)

/**
 * The usual calendar by variety. Today every variety shares the calendar of the valley ([USUAL]); a variety whose
 * dates a local specialist validated goes into [byVariety] and its plots use them without touching anything else.
 */
object PhaseProfiles {
    val USUAL = PhaseProfile(
        dormancyStarts = MonthDay.of(6, 1),
        budbreak = MonthDay.of(9, 1),
        fullBloom = MonthDay.of(10, 5),
        thinningOpens = MonthDay.of(11, 3),
        thinningCloses = MonthDay.of(11, 30),
        harvestStarts = MonthDay.of(4, 20),
        harvestEnds = MonthDay.of(6, 30),
    )

    private val byVariety: Map<OliveVariety, PhaseProfile> = emptyMap()

    fun forVariety(variety: OliveVariety?): PhaseProfile = byVariety[variety] ?: USUAL
}
