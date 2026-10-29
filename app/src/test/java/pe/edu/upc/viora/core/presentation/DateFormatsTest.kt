package pe.edu.upc.viora.core.presentation

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DateFormatsTest {

    private val spanish = Locale.forLanguageTag("es-PE")
    private val english = Locale.forLanguageTag("en-US")
    private val lima = ZoneId.of("America/Lima")

    @Test
    fun aDayIsWrittenWithoutTheDotOfTheShortMonth() {
        assertEquals("14 nov", formatDayMonth(LocalDate.of(2026, 11, 14), spanish))
        assertFalse(formatDayMonth(LocalDate.of(2026, 9, 20), spanish).contains('.'))
        assertEquals("14 Nov", formatDayMonth(LocalDate.of(2026, 11, 14), english))
    }

    @Test
    fun theMonthAloneHasNoDotEither() {
        assertEquals("nov", formatMonthShort(LocalDate.of(2026, 11, 1), spanish))
        assertEquals("Nov", formatMonthShort(LocalDate.of(2026, 11, 1), english))
    }

    @Test
    fun aRangeInsideOneMonthWritesTheMonthOnce() {
        assertEquals("3–30 nov", formatDayRange(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 30), spanish))
    }

    @Test
    fun aRangeAcrossTwoMonthsWritesBoth() {
        assertEquals("28 oct – 5 nov", formatDayRange(LocalDate.of(2026, 10, 28), LocalDate.of(2026, 11, 5), spanish))
        assertEquals("28 Oct – 5 Nov", formatDayRange(LocalDate.of(2026, 10, 28), LocalDate.of(2026, 11, 5), english))
    }

    @Test
    fun anInstantFallsOnTheDayOfTheGivenZone() {
        // 03:00 UTC on the 15th is still the 14th in Lima.
        val lateEvening = Instant.parse("2026-11-15T03:00:00Z")
        assertEquals("Nov 14, 2026", formatMediumDate(lateEvening, lima, english))
        assertEquals("Nov 15, 2026", formatMediumDate(lateEvening, ZoneId.of("UTC"), english))
    }

    @Test
    fun weekdaysAreCapitalizedWithoutDots() {
        val wednesday = LocalDate.of(2026, 10, 28)
        assertEquals("Mié", shortWeekday(wednesday, spanish))
        assertEquals("Wed", shortWeekday(wednesday, english))
        assertEquals("miércoles", fullWeekday(wednesday, spanish))
        assertEquals("Wednesday", fullWeekday(wednesday, english))
    }

    @Test
    fun daysLeftCountCalendarDaysAndNeverGoNegative() {
        val today = LocalDate.of(2026, 10, 29)
        assertEquals(3, daysUntil(today, LocalDate.of(2026, 11, 1)))
        assertEquals(0, daysUntil(today, today))
        assertEquals(0, daysUntil(today, LocalDate.of(2026, 10, 1)))
    }

    @Test
    fun thePickerDayRoundTripsThroughUtcMillis() {
        val day = LocalDate.of(2026, 5, 14)
        assertEquals(day, pickerMillisToLocalDate(day.toPickerMillis()))
        assertEquals(Instant.parse("2026-05-14T00:00:00Z").toEpochMilli(), day.toPickerMillis())
    }
}
