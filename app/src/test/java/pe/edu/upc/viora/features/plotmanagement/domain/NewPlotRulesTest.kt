package pe.edu.upc.viora.features.plotmanagement.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline

class NewPlotRulesTest {

    // ---- PlotName

    @Test
    fun `a plot name is trimmed and needs 3 to 100 characters`() {
        assertEquals("La Yarada", PlotName.of("  La Yarada  ").value)
        assertEquals(PlotName.Error.TOO_SHORT, PlotName.check(" ab "))
        assertEquals(PlotName.Error.TOO_SHORT, PlotName.check(""))
        assertEquals(PlotName.Error.TOO_LONG, PlotName.check("x".repeat(101)))
        assertNull(PlotName.check("abc"))
        assertNull(PlotName.check("x".repeat(100)))
    }

    @Test
    fun `an invalid plot name cannot be built`() {
        assertThrows(IllegalArgumentException::class.java) { PlotName.of("ab") }
    }

    // ---- PlantationFrame

    @Test
    fun `density follows 10000 divided by row times tree spacing`() {
        assertEquals(204, PlantationFrame(7.0, 7.0).treesPerHectare)
        assertEquals(286, PlantationFrame(7.0, 5.0).treesPerHectare)
    }

    @Test
    fun `a frame must be positive and not denser than 500 trees per hectare`() {
        assertNull(PlantationFrame.check(7.0, 7.0))
        assertEquals(PlantationFrame.Error.NOT_POSITIVE, PlantationFrame.check(0.0, 7.0))
        assertEquals(PlantationFrame.Error.NOT_POSITIVE, PlantationFrame.check(7.0, -1.0))
        assertEquals(PlantationFrame.Error.NOT_POSITIVE, PlantationFrame.check(Double.NaN, 7.0))
        // The exact case shown in the design: 1 x 7 m gives 1 429 trees per hectare.
        assertEquals(PlantationFrame.Error.TOO_DENSE, PlantationFrame.check(1.0, 7.0))
        assertThrows(IllegalArgumentException::class.java) { PlantationFrame(1.0, 7.0) }
    }

    // ---- PlotOutline

    private val square = listOf(
        GeoPoint(latitude = 0.000, longitude = 0.000),
        GeoPoint(latitude = 0.000, longitude = 0.001),
        GeoPoint(latitude = 0.001, longitude = 0.001),
        GeoPoint(latitude = 0.001, longitude = 0.000),
    )

    @Test
    fun `area of a 0_001 degree square near the equator is about 1_24 hectares`() {
        assertEquals(1.236, PlotOutline(square).areaHectares, 0.02)
    }

    @Test
    fun `area does not depend on the drawing direction`() {
        assertEquals(PlotOutline(square).areaHectares, PlotOutline(square.reversed()).areaHectares, 1e-9)
    }

    @Test
    fun `fewer than three corners is not a plot and reports how many are there`() {
        assertEquals(PlotOutline.Error.NotEnoughCorners(2), PlotOutline.check(square.take(2)))
        assertEquals(0.0, PlotOutline.areaHectares(square.take(2)), 0.0)
    }

    @Test
    fun `edges that cross are rejected`() {
        // A bow-tie: going 0,0 -> 1,1 -> 1,0 -> 0,1 makes two edges cross in the middle.
        val bowTie = listOf(
            GeoPoint(0.0, 0.0), GeoPoint(0.001, 0.001), GeoPoint(0.0, 0.001), GeoPoint(0.001, 0.0),
        )
        assertEquals(PlotOutline.Error.SelfIntersecting, PlotOutline.check(bowTie))
        assertThrows(IllegalArgumentException::class.java) { PlotOutline(bowTie) }
    }

    @Test
    fun `a simple convex or concave polygon is accepted`() {
        assertNull(PlotOutline.check(square))
        assertNull(PlotOutline.check(square.take(3)))
        val lShape = listOf(
            GeoPoint(0.000, 0.000), GeoPoint(0.000, 0.002), GeoPoint(0.001, 0.002),
            GeoPoint(0.001, 0.001), GeoPoint(0.002, 0.001), GeoPoint(0.002, 0.000),
        )
        assertNull(PlotOutline.check(lShape))
    }

    @Test
    fun `a corner to the right of a triangle is slipped into an edge so the outline stays simple`() {
        val triangle = listOf(
            GeoPoint(latitude = -18.0500, longitude = -70.2500),
            GeoPoint(latitude = -18.0495, longitude = -70.2490),
            GeoPoint(latitude = -18.0510, longitude = -70.2492),
        )
        // Appending it last would close across the first edge.
        val extra = GeoPoint(latitude = -18.0500, longitude = -70.2480)

        val index = PlotOutline.insertionIndex(triangle, extra)

        assertNotNull(index)
        val outline = triangle.toMutableList().apply { add(index!!, extra) }
        assertEquals(null, PlotOutline.check(outline))
    }

    @Test
    fun `a corner that keeps the outline simple goes last, in the order it was tapped`() {
        val square = listOf(
            GeoPoint(latitude = -18.050, longitude = -70.250),
            GeoPoint(latitude = -18.050, longitude = -70.249),
            GeoPoint(latitude = -18.051, longitude = -70.249),
        )

        assertEquals(3, PlotOutline.insertionIndex(square, GeoPoint(latitude = -18.051, longitude = -70.250)))
    }
}
