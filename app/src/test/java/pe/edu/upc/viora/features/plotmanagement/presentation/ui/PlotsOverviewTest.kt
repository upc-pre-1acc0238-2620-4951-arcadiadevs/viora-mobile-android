package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

class PlotsOverviewTest {
    private val west = listOf(
        GeoPoint(latitude = -18.050, longitude = -70.250),
        GeoPoint(latitude = -18.050, longitude = -70.249),
        GeoPoint(latitude = -18.051, longitude = -70.249),
    )
    private val east = west.map { GeoPoint(it.latitude, it.longitude + 0.002) }

    @Test
    fun `no outlines give an empty drawing`() {
        val drawing = projectOutlines(emptyList())

        assertTrue(drawing.shapes.isEmpty())
        assertEquals(0f, drawing.width, 0f)
    }

    @Test
    fun `plots keep their relative position in the drawing`() {
        val drawing = projectOutlines(listOf(west, east))

        // The eastern plot starts further to the right than the western one, at the same height.
        assertTrue(drawing.shapes[1][0].x > drawing.shapes[0][0].x)
        assertEquals(drawing.shapes[0][0].y, drawing.shapes[1][0].y, 1e-3f)
    }

    @Test
    fun `the drawing starts at the top left of the plots and is as wide as they are`() {
        val drawing = projectOutlines(listOf(west, east))

        assertEquals(0f, drawing.shapes.flatten().minOf { it.x }, 1e-3f)
        assertEquals(0f, drawing.shapes.flatten().minOf { it.y }, 1e-3f)
        assertEquals(drawing.shapes.flatten().maxOf { it.x }, drawing.width, 1e-2f)
    }
}
