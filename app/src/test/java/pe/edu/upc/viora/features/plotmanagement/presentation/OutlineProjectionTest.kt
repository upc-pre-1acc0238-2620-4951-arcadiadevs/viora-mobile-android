package pe.edu.upc.viora.features.plotmanagement.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.normalizeOutline

class OutlineProjectionTest {

    private val delta = 0.01f

    // A square near the equator, 0.01 degrees on each side.
    private val square = listOf(
        GeoPoint(latitude = 0.01, longitude = 0.00), // north-west
        GeoPoint(latitude = 0.01, longitude = 0.01), // north-east
        GeoPoint(latitude = 0.00, longitude = 0.01), // south-east
        GeoPoint(latitude = 0.00, longitude = 0.00), // south-west
    )

    @Test
    fun `north is up and east is right`() {
        val points = normalizeOutline(square)

        assertEquals(0f, points[0].x, delta)
        assertEquals(0f, points[0].y, delta)
        assertEquals(1f, points[1].x, delta)
        assertEquals(0f, points[1].y, delta)
        assertEquals(1f, points[2].x, delta)
        assertEquals(1f, points[2].y, delta)
        assertEquals(0f, points[3].x, delta)
        assertEquals(1f, points[3].y, delta)
    }

    @Test
    fun `a wide shape keeps its aspect ratio and is centred vertically`() {
        val wide = listOf(
            GeoPoint(latitude = 0.005, longitude = 0.00),
            GeoPoint(latitude = 0.005, longitude = 0.02),
            GeoPoint(latitude = 0.000, longitude = 0.02),
            GeoPoint(latitude = 0.000, longitude = 0.00),
        )

        val points = normalizeOutline(wide)

        // Width 0.02 and height 0.005: height is a quarter of the width, centred in the square.
        assertEquals(0.375f, points[0].y, delta)
        assertEquals(0.625f, points[3].y, delta)
        assertEquals(0f, points[0].x, delta)
        assertEquals(1f, points[1].x, delta)
    }

    @Test
    fun `every projected point stays inside the unit square`() {
        val points = normalizeOutline(square)

        assertTrue(points.all { it.x in 0f..1f && it.y in 0f..1f })
    }

    @Test
    fun `fewer than three points or a degenerate shape yields nothing`() {
        assertTrue(normalizeOutline(emptyList()).isEmpty())
        assertTrue(normalizeOutline(square.take(2)).isEmpty())
        assertTrue(normalizeOutline(List(3) { GeoPoint(1.0, 1.0) }).isEmpty())
    }
}
