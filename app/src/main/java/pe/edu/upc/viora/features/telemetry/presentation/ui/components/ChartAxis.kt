package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange

/** The vertical scale of a chart: [min]..[max] with a gridline every [step]. */
internal data class ChartAxis(val min: Double, val max: Double, val step: Double) {

    /** The values where a gridline is drawn, strictly inside the scale. */
    val gridValues: List<Double>
        get() = generateSequence(min + step) { it + step }.takeWhile { it < max - EPSILON }.toList()

    fun fractionOf(value: Double): Float = ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)

    companion object {
        private const val EPSILON = 1e-9
        private val STEPS = listOf(1.0, 2.0, 5.0, 10.0, 20.0, 50.0)

        /**
         * A scale that contains [lo]..[hi] with round limits and about [lines] gridlines, e.g.
         * 14..44 becomes 10..50 with a line every 10.
         */
        fun nice(lo: Double, hi: Double, lines: Int = 4): ChartAxis {
            val span = (hi - lo).coerceAtLeast(1.0)
            val step = STEPS.firstOrNull { span / it <= lines } ?: STEPS.last()
            val min = floor(lo / step) * step
            var max = ceil(hi / step) * step
            if (max - min < step) max = min + step
            return ChartAxis(min, max, step)
        }
    }
}

/** A label under the chart at [fraction] (0 = start of the window, 1 = now). */
internal data class AxisLabel(val fraction: Float, val text: String)

/**
 * The labels of the time axis for [range], ending at [now]: every 6 hours for 24 h, one weekday
 * per day for 7 days (centred on the day's noon) and a date every 5 days for 30 days.
 */
internal fun timeAxisLabels(range: TelemetryRange, now: Instant, zone: ZoneId, locale: Locale): List<AxisLabel> {
    val start = now.minus(Duration.ofHours(range.hours))
    val total = Duration.between(start, now).toMillis().toDouble()
    fun fractionOf(at: Instant): Float = (Duration.between(start, at).toMillis() / total).toFloat()
    fun inside(at: Instant) = at >= start && at <= now

    val firstDate = start.atZone(zone).toLocalDate()
    val lastDate = now.atZone(zone).toLocalDate()
    val dates = generateSequence(firstDate) { it.plusDays(1) }.takeWhile { !it.isAfter(lastDate) }.toList()

    return when (range) {
        TelemetryRange.LAST_24_HOURS -> dates.flatMap { date ->
            HOUR_MARKS.map { hour -> date.atTime(hour, 0).atZone(zone).toInstant() }
        }.filter(::inside).map { AxisLabel(fractionOf(it), formatHour(it, zone)) }

        TelemetryRange.LAST_7_DAYS -> dates.map { it.atTime(LocalTime.NOON).atZone(zone).toInstant() to it }
            .filter { (at, _) -> inside(at) }
            .map { (at, date) -> AxisLabel(fractionOf(at), shortWeekday(date, locale)) }

        TelemetryRange.LAST_30_DAYS -> dates.filterIndexed { index, _ -> index % DAYS_PER_LABEL == 0 }
            .map { it.atStartOfDay(zone).toInstant() to it }
            .filter { (at, _) -> inside(at) }
            .map { (at, date) -> AxisLabel(fractionOf(at), formatDayMonth(date, locale)) }
    }
}

private fun formatHour(at: Instant, zone: ZoneId): String = "%02d:00".format(at.atZone(zone).hour)

private val HOUR_MARKS = listOf(0, 6, 12, 18)
private const val DAYS_PER_LABEL = 5

/** Fraction of the window [range] ending at [now] where [at] falls (0 = start, 1 = now). */
internal fun windowFraction(at: Instant, range: TelemetryRange, now: Instant): Float {
    val start = now.minus(Duration.ofHours(range.hours))
    val total = Duration.between(start, now).toMillis().toDouble()
    return (Duration.between(start, at).toMillis() / total).toFloat().coerceIn(0f, 1f)
}
