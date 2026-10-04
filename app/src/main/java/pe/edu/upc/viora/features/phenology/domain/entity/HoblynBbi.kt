package pe.edu.upc.viora.features.phenology.domain.entity

import java.math.BigDecimal
import java.math.RoundingMode

/** Hoblyn index between two consecutive campaigns, [value] rounded to 2 decimals. */
data class BbiInterval(
    val fromYear: Int,
    val toYear: Int,
    val value: Double,
)

/**
 * Hoblyn biennial bearing index.
 *
 * For each pair of consecutive campaigns (a, b) the interval is |b - a| / (a + b), a value in
 * 0..1 (0 = steady yield, 1 = total alternation). The plot index is the mean of the intervals.
 * Pairs whose yields add up to zero are skipped. Results are rounded to 2 decimals (HALF_UP)
 * as the design shows them; the mean is computed from the unrounded intervals.
 */
object HoblynBbi {

    /** Minimum number of registered campaigns needed to show an index. */
    const val MIN_CAMPAIGNS = 3

    fun intervals(records: List<HarvestRecord>): List<BbiInterval> =
        rawIntervals(records).map { BbiInterval(it.first, it.second, round(it.third)) }

    fun index(records: List<HarvestRecord>): Double? {
        if (records.size < MIN_CAMPAIGNS) return null
        val raw = rawIntervals(records)
        if (raw.isEmpty()) return null
        return round(raw.sumOf { it.third } / raw.size)
    }

    private fun rawIntervals(records: List<HarvestRecord>): List<Triple<Int, Int, Double>> =
        records.sortedBy { it.campaignYear }
            .zipWithNext()
            .mapNotNull { (previous, next) ->
                val sum = previous.totalYieldKg + next.totalYieldKg
                if (sum == 0.0) {
                    null
                } else {
                    Triple(
                        previous.campaignYear,
                        next.campaignYear,
                        kotlin.math.abs(next.totalYieldKg - previous.totalYieldKg) / sum,
                    )
                }
            }

    private fun round(value: Double): Double =
        BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP).toDouble()
}
