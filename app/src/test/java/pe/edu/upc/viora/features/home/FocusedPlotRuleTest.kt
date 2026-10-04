package pe.edu.upc.viora.features.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.home.domain.FocusedPlotRule
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

class FocusedPlotRuleTest {

    private fun plot(id: String, name: String, hectares: Double) = Plot(
        id = PlotId(id),
        name = name,
        variety = OliveVariety.SEVILLANA,
        areaHectares = hectares,
        treesPerHectare = 72,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 4.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 0,
    )

    private val yarada = plot("1", "La Yarada 02", 2.5)
    private val norte = plot("2", "Lote Norte", 1.5)

    @Test
    fun `without plots there is nothing in focus`() {
        assertNull(FocusedPlotRule.pick(emptyList(), chosenId = null))
    }

    @Test
    fun `without a choice the biggest plot is in focus`() {
        assertEquals(yarada, FocusedPlotRule.pick(listOf(norte, yarada), chosenId = null))
    }

    @Test
    fun `the chosen plot wins over the biggest one`() {
        assertEquals(norte, FocusedPlotRule.pick(listOf(yarada, norte), chosenId = "2"))
    }

    @Test
    fun `a chosen plot that is gone falls back to the biggest one`() {
        assertEquals(yarada, FocusedPlotRule.pick(listOf(yarada, norte), chosenId = "archived"))
    }

    @Test
    fun `plots of the same size are told apart by name`() {
        val a = plot("3", "A lote", 2.0)
        val b = plot("4", "B lote", 2.0)

        assertEquals(a, FocusedPlotRule.pick(listOf(b, a), chosenId = null))
    }
}
