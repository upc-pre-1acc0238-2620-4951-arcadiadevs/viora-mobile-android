package pe.edu.upc.viora.features.phenology.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.core.domain.BloomHeat

/**
 * The calendar of the olive year in the producer's valley (La Yarada, southern hemisphere). The
 * campaign is the calendar year: the harvest of April-June and the bloom and thinning of
 * September-November of the same year belong to the same campaign. When a plot has its own dates
 * (the recorded full bloom, the thinning window of its plan) they replace the usual ones.
 */
object OliveCalendar {

    /** Above this maximum the bloom is at risk; the same threshold as the agroclimatic alerts ([BloomHeat]). */
    const val BLOOM_HEAT_CELSIUS = BloomHeat.CELSIUS

    /**
     * The dates of the [year] for a plot: those of its [profile], with the plot's own full bloom and thinning window
     * in their place when they are known and then the start dates the producer corrected ([adjustments]).
     *
     * What the plot's records fix wins over a correction. A phase that moves takes the phases around it with it: the
     * dormancy ends the day before the budbreak, the thinning window and the harvest keep their usual length, and a
     * correction that would break the order of the phases (after the records arrived) is held at the edge.
     */
    fun calendarFor(
        year: Int,
        fullBloomOn: LocalDate? = null,
        thinningOpensOn: LocalDate? = null,
        thinningClosesOn: LocalDate? = null,
        adjustments: PhaseAdjustments = PhaseAdjustments.NONE,
        profile: PhaseProfile = PhaseProfiles.USUAL,
    ): PhaseCalendar {
        val thinningLength = ChronoUnit.DAYS.between(profile.thinningOpens.atYear(year), profile.thinningCloses.atYear(year))
        val thinningOpens = thinningOpensOn
            ?: adjustments[OlivePhase.THINNING]?.atYear(year)?.let { if (fullBloomOn != null) maxOf(it, fullBloomOn.plusDays(1)) else it }
            ?: profile.thinningOpens.atYear(year)
        val thinningCloses = thinningClosesOn
            ?: if (adjustments[OlivePhase.THINNING] != null && thinningOpensOn == null) {
                thinningOpens.plusDays(thinningLength)
            } else {
                profile.thinningCloses.atYear(year)
            }
        val fullBloom = fullBloomOn
            ?: adjustments[OlivePhase.FRUIT_SET]?.atYear(year)?.let { minOf(it, thinningOpens.minusDays(1)) }
            ?: profile.fullBloom.atYear(year)
        val budbreak = adjustments[OlivePhase.BLOOM]?.atYear(year)?.let { minOf(it, fullBloom.minusDays(1)) }
            ?: profile.budbreak.atYear(year)
        val dormancyEnds = budbreak.minusDays(1)
        val dormancyStarts = adjustments[OlivePhase.DORMANCY]?.atYear(year)?.let { minOf(it, dormancyEnds.minusDays(1)) }
            ?: profile.dormancyStarts.atYear(year)
        val harvestLength = ChronoUnit.DAYS.between(profile.harvestStarts.atYear(year), profile.harvestEnds.atYear(year))
        val harvestAdjusted = adjustments[OlivePhase.HARVEST]?.atYear(year)
        val harvestStarts = harvestAdjusted ?: profile.harvestStarts.atYear(year)
        val harvestEnds = if (harvestAdjusted != null) harvestStarts.plusDays(harvestLength) else profile.harvestEnds.atYear(year)
        return PhaseCalendar(
            dormancyStarts = dormancyStarts,
            dormancyEnds = dormancyEnds,
            budbreak = budbreak,
            fullBloom = fullBloom,
            thinningOpens = thinningOpens,
            thinningCloses = thinningCloses,
            harvestStarts = harvestStarts,
            harvestEnds = harvestEnds,
            bloomHeatCelsius = BLOOM_HEAT_CELSIUS,
        )
    }

    /**
     * The days the start of [phase] can be moved to in the [year], given the [calendar] of the plot as it stands: from
     * the day after the start of the phase before it to the day before the start of the one after, so the phases keep
     * their order. The thinning window and the harvest keep their length, so they can start until that fits the year.
     */
    fun startBounds(year: Int, phase: OlivePhase, calendar: PhaseCalendar, profile: PhaseProfile = PhaseProfiles.USUAL): DateBounds {
        val firstDay = LocalDate.of(year, 1, 1)
        val lastDay = LocalDate.of(year, 12, 31)
        val thinningLength = ChronoUnit.DAYS.between(profile.thinningOpens.atYear(year), profile.thinningCloses.atYear(year))
        val harvestLength = ChronoUnit.DAYS.between(profile.harvestStarts.atYear(year), profile.harvestEnds.atYear(year))
        return when (phase) {
            OlivePhase.DORMANCY -> DateBounds(firstDay, calendar.budbreak.minusDays(2))
            OlivePhase.BLOOM -> DateBounds(calendar.dormancyStarts.plusDays(1), calendar.fullBloom.minusDays(1))
            OlivePhase.FRUIT_SET -> DateBounds(calendar.budbreak.plusDays(1), calendar.thinningOpens.minusDays(1))
            OlivePhase.THINNING -> DateBounds(calendar.fullBloom.plusDays(1), lastDay.minusDays(thinningLength))
            OlivePhase.HARVEST -> DateBounds(firstDay, lastDay.minusDays(harvestLength))
        }
    }
}

/** The dated milestones of one olive year for one plot (see [OliveCalendar.calendarFor]). */
data class PhaseCalendar(
    val dormancyStarts: LocalDate,
    val dormancyEnds: LocalDate,
    val budbreak: LocalDate,
    val fullBloom: LocalDate,
    val thinningOpens: LocalDate,
    val thinningCloses: LocalDate,
    val harvestStarts: LocalDate,
    val harvestEnds: LocalDate,
    /** Above this maximum during the bloom the producer is asked to water early. */
    val bloomHeatCelsius: Double,
)
