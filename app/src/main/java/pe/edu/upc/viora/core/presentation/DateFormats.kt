package pe.edu.upc.viora.core.presentation

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

// The one way the app writes dates. Short months never keep the dot some locales add ("14 nov", not
// "14 nov."), as every screen in Figma writes them. Instants are shown in the zone the caller passes:
// screens read it from LocalAppZone, view models from the injected ZoneId.

/** "14 nov" in Spanish, "14 Nov" in English: a day of the month. */
fun formatDayMonth(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofPattern("d MMM", locale).format(date).withoutDots()

/** "nov": the short month alone, for chart axes. */
fun formatMonthShort(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofPattern("MMM", locale).format(date).withoutDots()

/** "3–30 nov" within one month, "28 oct – 5 nov" across two. */
fun formatDayRange(opens: LocalDate, closes: LocalDate, locale: Locale = Locale.getDefault()): String =
    if (opens.month == closes.month && opens.year == closes.year) {
        "${opens.dayOfMonth}–${formatDayMonth(closes, locale)}"
    } else {
        "${formatDayMonth(opens, locale)} – ${formatDayMonth(closes, locale)}"
    }

/** "14 mar 2025": the day an [instant] fell on in [zone], in the language's medium style. */
fun formatMediumDate(instant: Instant, zone: ZoneId, locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(instant.atZone(zone))

/** "6:40 a. m." in the language and [zone] of the producer. */
fun formatClock(at: Instant, zone: ZoneId, locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).format(at.atZone(zone))

/** "Mié" / "Wed": the short weekday, capitalized and without a trailing dot. */
fun shortWeekday(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).trimEnd('.').capitalized(locale)

/** "miércoles" / "Wednesday". */
fun fullWeekday(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)

/** Whole days from [from] to [to], never negative: "days left" counts calendar days, not 24-hour spans. */
fun daysUntil(from: LocalDate, to: LocalDate): Int = ChronoUnit.DAYS.between(from, to).toInt().coerceAtLeast(0)

/** The Material date picker works in UTC-midnight milliseconds; this is the day it should select. */
fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** The day the Material date picker returned as UTC-midnight [millis]. */
fun pickerMillisToLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

private fun String.withoutDots(): String = replace(".", "")

private fun String.capitalized(locale: Locale): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
