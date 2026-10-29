package pe.edu.upc.viora.features.phenology.domain

import java.time.LocalDate
import java.time.MonthDay
import pe.edu.upc.viora.core.domain.BloomHeat

/**
 * The calendar of the olive year in the producer's valley (La Yarada, southern hemisphere). The
 * campaign is the calendar year: the harvest of April-June and the bloom and thinning of
 * September-November of the same year belong to the same campaign. When a plot has its own dates
 * (the recorded full bloom, the thinning window of its plan) they replace the usual ones.
 */
object OliveCalendar {
    val DORMANCY_STARTS: MonthDay = MonthDay.of(6, 1)
    val DORMANCY_ENDS: MonthDay = MonthDay.of(8, 31)
    val BUDBREAK: MonthDay = MonthDay.of(9, 1)
    val USUAL_FULL_BLOOM: MonthDay = MonthDay.of(10, 5)
    val USUAL_THINNING_WINDOW_OPENS: MonthDay = MonthDay.of(11, 3)
    val USUAL_THINNING_WINDOW_CLOSES: MonthDay = MonthDay.of(11, 30)
    val HARVEST_STARTS: MonthDay = MonthDay.of(4, 20)
    val HARVEST_ENDS: MonthDay = MonthDay.of(6, 30)

    /** Above this maximum the bloom is at risk; the same threshold as the agroclimatic alerts ([BloomHeat]). */
    const val BLOOM_HEAT_CELSIUS = BloomHeat.CELSIUS

    /**
     * The dates of the [year] for a plot: the usual ones, with the plot's own full bloom and thinning window in their
     * place when they are known.
     */
    fun calendarFor(
        year: Int,
        fullBloomOn: LocalDate? = null,
        thinningOpensOn: LocalDate? = null,
        thinningClosesOn: LocalDate? = null,
    ): PhaseCalendar = PhaseCalendar(
        dormancyStarts = DORMANCY_STARTS.atYear(year),
        dormancyEnds = DORMANCY_ENDS.atYear(year),
        budbreak = BUDBREAK.atYear(year),
        fullBloom = fullBloomOn ?: USUAL_FULL_BLOOM.atYear(year),
        thinningOpens = thinningOpensOn ?: USUAL_THINNING_WINDOW_OPENS.atYear(year),
        thinningCloses = thinningClosesOn ?: USUAL_THINNING_WINDOW_CLOSES.atYear(year),
        harvestStarts = HARVEST_STARTS.atYear(year),
        harvestEnds = HARVEST_ENDS.atYear(year),
        bloomHeatCelsius = BLOOM_HEAT_CELSIUS,
    )
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
