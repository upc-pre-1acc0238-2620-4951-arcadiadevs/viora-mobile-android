package pe.edu.upc.viora.features.telemetry.presentation

import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ChartAxis
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatElapsed
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.timeAxisLabels

class ChartAxisTest {

    @Test
    fun `the scale contains the data with round limits`() {
        val axis = ChartAxis.nice(14.0, 44.0)
        assertEquals(10.0, axis.min, 0.0)
        assertEquals(50.0, axis.max, 0.0)
        assertEquals(10.0, axis.step, 0.0)
        assertEquals(listOf(20.0, 30.0, 40.0), axis.gridValues)
    }

    @Test
    fun `a flat series still gets a usable scale`() {
        val axis = ChartAxis.nice(30.0, 30.0)
        assertTrue(axis.max > axis.min)
    }

    @Test
    fun `fractions stay inside the scale`() {
        val axis = ChartAxis(10.0, 50.0, 10.0)
        assertEquals(0.5f, axis.fractionOf(30.0), 0.0001f)
        assertEquals(0f, axis.fractionOf(0.0), 0f)
        assertEquals(1f, axis.fractionOf(99.0), 0f)
    }

    @Test
    fun `the week axis has one weekday label per day`() {
        val now = Instant.parse("2026-10-06T15:30:00Z")
        val labels = timeAxisLabels(TelemetryRange.LAST_7_DAYS, now, ZoneOffset.UTC, Locale.ENGLISH)
        assertEquals(listOf("Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue"), labels.map { it.text })
        assertTrue(labels.all { it.fraction in 0f..1f })
    }

    @Test
    fun `the day axis labels every six hours`() {
        val now = Instant.parse("2026-10-06T15:30:00Z")
        val labels = timeAxisLabels(TelemetryRange.LAST_24_HOURS, now, ZoneOffset.UTC, Locale.ENGLISH)
        assertEquals(listOf("18:00", "00:00", "06:00", "12:00"), labels.map { it.text })
    }

    @Test
    fun `elapsed time is shown in minutes, hours or days`() {
        val now = Instant.parse("2026-10-06T15:30:00Z")
        assertEquals("1 min", formatElapsed(now.minusSeconds(10), now))
        assertEquals("10 min", formatElapsed(now.minusSeconds(600), now))
        assertEquals("3 h", formatElapsed(now.minusSeconds(3 * 3600L), now))
        assertEquals("2 d", formatElapsed(now.minusSeconds(2 * 86_400L), now))
        assertEquals("1 min", formatElapsed(now.plusSeconds(60), now))
    }
}
