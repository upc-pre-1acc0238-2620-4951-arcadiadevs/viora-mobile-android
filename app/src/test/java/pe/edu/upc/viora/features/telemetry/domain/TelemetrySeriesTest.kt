package pe.edu.upc.viora.features.telemetry.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.domain.entity.HourlyReading
import pe.edu.upc.viora.features.telemetry.domain.entity.TelemetrySeries
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SoilMoistureStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric

class TelemetrySeriesTest {

    private val utc: ZoneId = ZoneOffset.UTC
    private val midnight = Instant.parse("2026-10-01T00:00:00Z")

    private fun reading(hour: Long, temp: Double? = null, humidity: Double? = null, soil: Double? = null) =
        HourlyReading(midnight.plus(Duration.ofHours(hour)), temp, humidity, soil, null)

    @Test
    fun `an empty series has no latest value`() {
        val series = TelemetrySeries("p", emptyList())
        assertTrue(series.isEmpty)
        assertNull(series.latest(TelemetryMetric.TEMPERATURE))
        assertNull(series.stats(TelemetryMetric.TEMPERATURE))
        assertNull(series.soilStatus())
    }

    @Test
    fun `the latest value skips hours the metric was not reported`() {
        val series = TelemetrySeries("p", listOf(reading(0, soil = 30.0), reading(1, soil = 29.0), reading(2, temp = 20.0)))
        val latest = series.latest(TelemetryMetric.SOIL_MOISTURE)
        assertEquals(29.0, latest!!.value, 0.0)
        assertEquals(midnight.plus(Duration.ofHours(1)), latest.observedAt)
    }

    @Test
    fun `stats give the minimum, maximum and average with their moments`() {
        val series = TelemetrySeries("p", listOf(reading(0, soil = 30.0), reading(1, soil = 20.0), reading(2, soil = 40.0)))
        val stats = series.stats(TelemetryMetric.SOIL_MOISTURE)!!
        assertEquals(20.0, stats.min.value, 0.0)
        assertEquals(midnight.plus(Duration.ofHours(1)), stats.min.observedAt)
        assertEquals(40.0, stats.max.value, 0.0)
        assertEquals(30.0, stats.average, 0.0)
    }

    @Test
    fun `soil status follows the latest reading`() {
        val stressed = TelemetrySeries("p", listOf(reading(0, soil = 30.0), reading(1, soil = 15.0)))
        assertEquals(SoilMoistureStatus.STRESS, stressed.soilStatus())
    }

    @Test
    fun `day and night averages are split at the daylight hours`() {
        // 06:00-17:59 is day. Day readings 30 and 28, night readings 12 and 14.
        val series = TelemetrySeries(
            "p",
            listOf(
                reading(2, temp = 12.0), reading(6, temp = 30.0), reading(12, temp = 28.0), reading(20, temp = 14.0),
            ),
        )
        val summary = series.thermalSummary(utc)!!
        assertEquals(29.0, summary.dayAverageCelsius, 0.0)
        assertEquals(13.0, summary.nightAverageCelsius, 0.0)
        assertEquals(16.0, summary.oscillationCelsius, 0.0)
    }

    @Test
    fun `thermal summary needs both day and night readings`() {
        val dayOnly = TelemetrySeries("p", listOf(reading(8, temp = 25.0), reading(10, temp = 27.0)))
        assertNull(dayOnly.thermalSummary(utc))
    }

    @Test
    fun `daily summaries are grouped by calendar day in the plot zone`() {
        val series = TelemetrySeries(
            "p",
            listOf(reading(8, temp = 26.0), reading(22, temp = 12.0), reading(32, temp = 28.0), reading(46, temp = 14.0)),
        )
        val days = series.dailyThermalSummaries(utc)
        assertEquals(listOf(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2)), days.map { it.date })
        assertEquals(26.0, days[0].dayAverageCelsius!!, 0.0)
        assertEquals(12.0, days[0].nightAverageCelsius!!, 0.0)
        assertNotNull(days[1].dayAverageCelsius)
    }

    @Test
    fun `the zone moves hours between day and night`() {
        // 12:00 UTC is 07:00 in Lima (day) but 21:00 in Tokyo (night); 00:00 UTC is 09:00 in Tokyo (day).
        val series = TelemetrySeries("p", listOf(reading(12, temp = 25.0), reading(0, temp = 10.0)))
        val lima = series.dailyThermalSummaries(ZoneId.of("America/Lima"))
        assertEquals(25.0, lima.last().dayAverageCelsius!!, 0.0)
        val tokyo = series.dailyThermalSummaries(ZoneId.of("Asia/Tokyo"))
        assertEquals(10.0, tokyo.last().dayAverageCelsius!!, 0.0)
        assertEquals(25.0, tokyo.last().nightAverageCelsius!!, 0.0)
    }
}
