package pe.edu.upc.viora.features.plotmanagement.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

class PlotTest {

    private fun plot(area: Double, density: Int) = Plot(
        id = PlotId("p-1"),
        name = "La Yarada 02",
        variety = OliveVariety.SEVILLANA,
        areaHectares = area,
        treesPerHectare = density,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 5.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 0,
    )

    @Test
    fun `estimated trees multiplies area by density and rounds`() {
        assertEquals(180, plot(area = 2.5, density = 72).estimatedTrees)
        assertEquals(188, plot(area = 0.92, density = 204).estimatedTrees)
    }

    @Test
    fun `plot id rejects blank values`() {
        assertThrows(IllegalArgumentException::class.java) { PlotId("  ") }
    }

    @Test
    fun `geo point rejects out of range coordinates`() {
        assertThrows(IllegalArgumentException::class.java) { GeoPoint(latitude = 91.0, longitude = 0.0) }
        assertThrows(IllegalArgumentException::class.java) { GeoPoint(latitude = 0.0, longitude = 181.0) }
    }
}
