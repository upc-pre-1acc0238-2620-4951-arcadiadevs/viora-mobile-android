package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.core.presentation.formatWhole

// Formatting of the plot climate screens. Pure functions of (value, locale) so they can be tested.

/** "27°" or, with [decimals] = 1, "27,4°" (the app's decimal comma in Spanish). */
internal fun formatDegrees(locale: Locale, value: Double, decimals: Int = 0): String =
    formatDecimal(value, decimals, locale) + "°"

/** "34 %": the percent sign is separated by a space, as in the design. */
internal fun formatPercent(locale: Locale, value: Double): String = formatWhole(value, locale) + "\u00A0%"

/**
 * A reading with the unit the backend sends, written as the design does: "34°" for "°C",
 * "16 %" for "%", otherwise "2,5 kPa". Whole values drop their decimals.
 */
internal fun formatMetricValue(locale: Locale, value: Double, unit: String): String {
    val decimals = if (value % 1.0 == 0.0) 0 else 1
    return when (unit.trim()) {
        "°C", "°" -> formatDegrees(locale, value, decimals)
        "%" -> formatDecimal(value, decimals, locale) + "\u00A0%"
        else -> formatDecimal(value, decimals, locale) + "\u00A0" + unit.trim()
    }
}

internal fun formatWholeNumber(locale: Locale, value: Double): String = formatWhole(value, locale)

/** "10 min", "3 h" or "2 d" between [from] and [now] (never negative, at least one minute). */
internal fun formatElapsed(from: Instant, now: Instant): String {
    val elapsed = Duration.between(from, now).coerceAtLeast(Duration.ZERO)
    return when {
        elapsed.toMinutes() < MINUTES_PER_HOUR -> "${maxOf(elapsed.toMinutes(), 1)} min"
        elapsed.toHours() < HOURS_PER_DAY -> "${elapsed.toHours()} h"
        else -> "${elapsed.toDays()} d"
    }
}

/** "Mié" / "Wed": the short weekday without the trailing dot some locales add. */
internal fun shortWeekday(date: LocalDate, locale: Locale): String =
    date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).trimEnd('.').capitalized(locale)

/** "miércoles" / "Wednesday". */
internal fun fullWeekday(date: LocalDate, locale: Locale): String =
    date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)

/** "6:40 a. m." in the producer's language and time zone. */
internal fun formatClock(at: Instant, zone: ZoneId, locale: Locale): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).format(at.atZone(zone))

/** "Martes 18, 10:30 a. m.": the stamp under the last reading. */
internal fun formatReadingStamp(at: Instant, zone: ZoneId, locale: Locale): String {
    val local = at.atZone(zone)
    val weekday = local.dayOfWeek.getDisplayName(TextStyle.FULL, locale).capitalized(locale)
    return "$weekday ${local.dayOfMonth}, ${formatClock(at, zone, locale)}"
}

/** "18 sep": the label of the 30-day axis. */
internal fun formatDayMonth(date: LocalDate, locale: Locale): String =
    DateTimeFormatter.ofPattern("d MMM", locale).format(date).replace(".", "")

private fun String.capitalized(locale: Locale): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

private const val MINUTES_PER_HOUR = 60
private const val HOURS_PER_DAY = 24
