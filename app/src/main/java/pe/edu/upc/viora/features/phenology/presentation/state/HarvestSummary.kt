package pe.edu.upc.viora.features.phenology.presentation.state

import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi

/** Heading of the screen, picked from how strongly the plot alternates. */
enum class Headline { STRONG, MODERATE, STEADY, MISSING }

/** What the next campaign is expected to be. */
enum class Outlook { OFF_LIKELY, ON_LIKELY, STEADY }

/** The one sentence Viora says on the screen. [year] is the campaign it talks about. */
sealed interface Voice {
    data class BreakCycle(val year: Int) : Voice
    data class Recover(val year: Int) : Voice
    data object Steady : Voice
    data class AskYear(val year: Int) : Voice
}

/** A yield that fell after an ON year: [ratio] is [afterKg] / [fromKg]. */
data class DropAfterOn(val fromKg: Double, val afterKg: Double) {
    val ratio: Double get() = afterKg / fromKg
}

/**
 * Everything the alternation screen says in words, derived from the records. [missing] is how
 * many campaigns are still needed for an index (0 when there is one).
 */
data class HarvestSummary(
    val headline: Headline,
    val missing: Int,
    val voice: Voice,
    val dropAfterOn: DropAfterOn?,
    val nextYear: Int?,
    val outlook: Outlook?,
)

/** The first pair of campaigns, worked out step by step in the "what is the index" sheet. */
data class BbiExample(val fromYear: Int, val toYear: Int, val fromKg: Double, val toKg: Double, val value: Double) {
    val differenceKg: Double get() = kotlin.math.abs(toKg - fromKg)
    val sumKg: Double get() = fromKg + toKg
}

/** Pure rules behind the copy of the screen; no Android, no formatting. */
object HarvestPresenter {

    /** Year the add sheet starts on: one before the oldest campaign, else last year. */
    fun suggestedYear(records: List<HarvestRecord>, currentYear: Int): Int =
        records.minOfOrNull { it.campaignYear }?.minus(1) ?: (currentYear - 1)

    /** The [missing] campaigns still needed, oldest first, counting back from [suggestedYear]. */
    fun missingYears(missing: Int, suggestedYear: Int): List<Int> =
        (missing - 1 downTo 0).map { suggestedYear - it }

    /** [bbiClass] is null when there is no index (fewer than [HoblynBbi.MIN_CAMPAIGNS] campaigns). */
    fun summarize(records: List<HarvestRecord>, bbiClass: BbiClass?, currentYear: Int): HarvestSummary {
        if (bbiClass == null) {
            return HarvestSummary(
                headline = Headline.MISSING,
                missing = (HoblynBbi.MIN_CAMPAIGNS - records.size).coerceAtLeast(1),
                voice = Voice.AskYear(suggestedYear(records, currentYear)),
                dropAfterOn = null,
                nextYear = null,
                outlook = null,
            )
        }
        val ordered = records.sortedBy { it.campaignYear }
        val last = ordered.last()
        val steady = bbiClass == BbiClass.LOW
        val outlook = when {
            steady -> Outlook.STEADY
            last.bearing == BearingYear.ON -> Outlook.OFF_LIKELY
            last.bearing == BearingYear.OFF -> Outlook.ON_LIKELY
            else -> Outlook.STEADY
        }
        val nextYear = last.campaignYear + 1
        return HarvestSummary(
            headline = when (bbiClass) {
                BbiClass.SEVERE -> Headline.STRONG
                BbiClass.MODERATE -> Headline.MODERATE
                BbiClass.LOW -> Headline.STEADY
            },
            missing = 0,
            voice = when (outlook) {
                Outlook.OFF_LIKELY -> Voice.BreakCycle(nextYear)
                Outlook.ON_LIKELY -> Voice.Recover(nextYear)
                Outlook.STEADY -> Voice.Steady
            },
            dropAfterOn = if (steady) null else lastDropAfterOn(ordered),
            nextYear = nextYear,
            outlook = outlook,
        )
    }

    /** The oldest interval of the plot, or null when there is none to show. */
    fun example(records: List<HarvestRecord>): BbiExample? {
        val interval = HoblynBbi.intervals(records).firstOrNull() ?: return null
        val from = records.first { it.campaignYear == interval.fromYear }
        val to = records.first { it.campaignYear == interval.toYear }
        return BbiExample(interval.fromYear, interval.toYear, from.totalYieldKg, to.totalYieldKg, interval.value)
    }

    private fun lastDropAfterOn(ordered: List<HarvestRecord>): DropAfterOn? =
        ordered.zipWithNext()
            .lastOrNull { (a, b) -> a.bearing == BearingYear.ON && b.bearing == BearingYear.OFF && a.totalYieldKg > 0 }
            ?.let { (a, b) -> DropAfterOn(fromKg = a.totalYieldKg, afterKg = b.totalYieldKg) }
}
