package pe.edu.upc.viora.features.phenology.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker

/** The five phases of the olive year, in the order the year walks through them. */
enum class OlivePhase { DORMANCY, BLOOM, FRUIT_SET, THINNING, HARVEST }

/**
 * Where today falls against a phase: the current one, one still ahead or one already behind. [LATE] is a phase
 * already behind with work still open (a harvest not registered after June).
 */
enum class PhaseTiming { NOW, NEXT, DONE, LATE }

/**
 * What other contexts know about a plot that moves its phases: the full bloom recorded for it and the window of its
 * thinning plan (each null while unknown), and whether some plot still has its harvest to register.
 */
data class PlotPhaseDates(
    val fullBloomOn: LocalDate? = null,
    val thinningOpensOn: LocalDate? = null,
    val thinningClosesOn: LocalDate? = null,
    val harvestPending: Boolean = false,
)

/**
 * One phase of the year for a plot: the two dates that bound it, where today falls against it and how much of it
 * has gone by (0..1; for the dormancy, the share of the chill already gathered once it began).
 */
data class PhaseWindow(
    val phase: OlivePhase,
    val startsOn: LocalDate,
    val endsOn: LocalDate,
    val timing: PhaseTiming,
    val progress: Float,
)

/** The olive year of a plot as of [today]: its phases in order and the one going on, [current]. */
data class CurrentPhase(
    val today: LocalDate,
    val calendar: PhaseCalendar,
    val windows: List<PhaseWindow>,
    val current: OlivePhase,
    /** The plot's winter chill, when it was measured. */
    val chill: ChillTracker?,
    /** True once the plot's own full bloom is recorded: until then its thinning window is the usual one. */
    val bloomRecorded: Boolean,
) {
    fun window(phase: OlivePhase): PhaseWindow = windows[phase.ordinal]

    /** A maximum that puts the bloom at risk: the producer is asked to water early. */
    fun isBloomHeat(maxCelsius: Double): Boolean = maxCelsius >= calendar.bloomHeatCelsius
}

/** Which phase of the olive year a plot is in, from its calendar (see [OliveCalendar]) and its own data. */
object OlivePhases {

    fun of(today: LocalDate, dates: PlotPhaseDates, chill: ChillTracker?): CurrentPhase {
        val calendar = OliveCalendar.calendarFor(today.year, dates.fullBloomOn, dates.thinningOpensOn, dates.thinningClosesOn)
        val windows = listOf(
            dormancy(today, calendar, chill),
            window(OlivePhase.BLOOM, today, calendar.budbreak, calendar.fullBloom),
            // The fruit set ends the day before the thinning window opens.
            window(OlivePhase.FRUIT_SET, today, calendar.fullBloom, calendar.thinningOpens, lastDay = calendar.thinningOpens.minusDays(1)),
            window(OlivePhase.THINNING, today, calendar.thinningOpens, calendar.thinningCloses),
            harvest(today, calendar, dates.harvestPending),
        )
        return CurrentPhase(
            today = today,
            calendar = calendar,
            windows = windows,
            current = current(windows, dates.harvestPending),
            chill = chill,
            bloomRecorded = dates.fullBloomOn != null,
        )
    }

    /**
     * The phase going on today. In June the harvest and the winter overlap; the harvest wins while a plot is still to
     * register. Between two phases (December to April) it is the next one.
     */
    private fun current(windows: List<PhaseWindow>, harvestPending: Boolean): OlivePhase {
        val now = windows.filter { it.timing == PhaseTiming.NOW }.map { it.phase }
        if (OlivePhase.HARVEST in now && harvestPending) return OlivePhase.HARVEST
        now.firstOrNull { it != OlivePhase.HARVEST }?.let { return it }
        now.firstOrNull()?.let { return it }
        return windows.filter { it.timing == PhaseTiming.NEXT }.minByOrNull { it.startsOn }?.phase ?: OlivePhase.HARVEST
    }

    private fun dormancy(today: LocalDate, calendar: PhaseCalendar, chill: ChillTracker?): PhaseWindow {
        val base = window(OlivePhase.DORMANCY, today, calendar.dormancyStarts, calendar.dormancyEnds)
        if (base.timing == PhaseTiming.NEXT) return base.copy(progress = 0f)
        if (chill == null) return base
        val gathered = if (chill.thresholdPortions > 0) {
            (chill.accumulatedPortions / chill.thresholdPortions).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
        return base.copy(progress = gathered)
    }

    /** A harvest still to register after its season is late, not done. */
    private fun harvest(today: LocalDate, calendar: PhaseCalendar, pending: Boolean): PhaseWindow {
        val base = window(OlivePhase.HARVEST, today, calendar.harvestStarts, calendar.harvestEnds)
        return if (pending && base.timing == PhaseTiming.DONE) base.copy(timing = PhaseTiming.LATE) else base
    }

    private fun window(phase: OlivePhase, today: LocalDate, starts: LocalDate, ends: LocalDate, lastDay: LocalDate = ends) =
        PhaseWindow(phase, starts, ends, timing(today, starts, lastDay), elapsed(today, starts, ends))

    private fun timing(today: LocalDate, starts: LocalDate, lastDay: LocalDate): PhaseTiming = when {
        today.isBefore(starts) -> PhaseTiming.NEXT
        today.isAfter(lastDay) -> PhaseTiming.DONE
        else -> PhaseTiming.NOW
    }

    private fun elapsed(today: LocalDate, starts: LocalDate, ends: LocalDate): Float {
        val total = ChronoUnit.DAYS.between(starts, ends).coerceAtLeast(1)
        return (ChronoUnit.DAYS.between(starts, today).toFloat() / total).coerceIn(0f, 1f)
    }
}
