package pe.edu.upc.viora.features.telemetry.domain

import java.time.Duration
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.domain.entity.MetricPoint
import pe.edu.upc.viora.features.telemetry.domain.entity.SoilMoistureRules
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SoilMoistureStatus

class SoilMoistureRulesTest {

    private val start = Instant.parse("2026-10-01T00:00:00Z")

    private fun hourly(vararg values: Double) =
        values.mapIndexed { index, v -> MetricPoint(start.plus(Duration.ofHours(index.toLong())), v) }

    @Test
    fun `below the recharge point is stress`() {
        assertEquals(SoilMoistureStatus.STRESS, SoilMoistureRules.statusOf(17.9))
    }

    @Test
    fun `exactly the recharge point is not stress yet`() {
        assertEquals(SoilMoistureStatus.WATCH, SoilMoistureRules.statusOf(18.0))
    }

    @Test
    fun `between the recharge point and the watch level is watch`() {
        assertEquals(SoilMoistureStatus.WATCH, SoilMoistureRules.statusOf(22.0))
    }

    @Test
    fun `above the watch level is in range`() {
        assertEquals(SoilMoistureStatus.IN_RANGE, SoilMoistureRules.statusOf(34.0))
    }

    @Test
    fun `a sharp rise between close readings is an irrigation`() {
        val points = hourly(20.0, 19.9, 30.0, 29.9)
        assertEquals(listOf(points[2].observedAt), SoilMoistureRules.irrigationEvents(points))
    }

    @Test
    fun `a slow rise is not an irrigation`() {
        assertTrue(SoilMoistureRules.irrigationEvents(hourly(20.0, 21.0, 22.0, 23.0)).isEmpty())
    }

    @Test
    fun `a jump across a long gap is not an irrigation`() {
        val points = listOf(
            MetricPoint(start, 20.0),
            MetricPoint(start.plus(Duration.ofHours(10)), 30.0),
        )
        assertTrue(SoilMoistureRules.irrigationEvents(points).isEmpty())
    }

    @Test
    fun `projects when the soil reaches the watch level at its current pace`() {
        // 34 % falling 0.5 points per hour: 12 points above the 22 % level, so 24 hours left.
        val points = hourly(*DoubleArray(10) { 36.0 - it * 0.5 }) // last value 31.5 -> 9.5 over -> 19 h
        val eta = SoilMoistureRules.projectWatchLevel(points)
        assertNotNull(eta)
        assertEquals(points.last().observedAt.plus(Duration.ofHours(19)), eta)
    }

    @Test
    fun `only the readings since the last irrigation drive the projection`() {
        val beforeIrrigation = DoubleArray(6) { 40.0 - it * 2.0 }
        val afterIrrigation = DoubleArray(8) { 38.0 - it * 0.5 }
        val eta = SoilMoistureRules.projectWatchLevel(hourly(*beforeIrrigation, *afterIrrigation))
        assertNotNull(eta)
        // 34.5 now, 12.5 above 22, 0.5 per hour -> 25 h.
        val last = start.plus(Duration.ofHours(13))
        assertEquals(last.plus(Duration.ofHours(25)), eta)
    }

    @Test
    fun `no projection when the soil is not drying`() {
        assertNull(SoilMoistureRules.projectWatchLevel(hourly(30.0, 30.0, 30.1, 30.0, 30.1, 30.2, 30.3)))
    }

    @Test
    fun `no projection when already at or below the watch level`() {
        assertNull(SoilMoistureRules.projectWatchLevel(hourly(24.0, 23.5, 23.0, 22.5, 22.0, 21.5, 21.0)))
    }

    @Test
    fun `no projection with too few readings`() {
        assertNull(SoilMoistureRules.projectWatchLevel(hourly(34.0, 33.0, 32.0)))
    }

    @Test
    fun `no projection when the date is more than a week away`() {
        assertNull(SoilMoistureRules.projectWatchLevel(hourly(*DoubleArray(10) { 40.0 - it * 0.02 })))
    }
}
