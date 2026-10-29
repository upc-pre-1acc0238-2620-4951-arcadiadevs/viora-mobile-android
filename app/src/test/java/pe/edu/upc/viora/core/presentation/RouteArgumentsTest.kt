package pe.edu.upc.viora.core.presentation

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import pe.edu.upc.viora.core.domain.PlotId

/** Every route reads its plot through [PlotId]: one rule for a plot id wherever it enters. */
class RouteArgumentsTest {

    @Test
    fun `the plot of a route is a plot id`() {
        assertEquals(PlotId("plot-1"), SavedStateHandle(mapOf("plotId" to "plot-1")).requirePlotId())
    }

    @Test
    fun `a route without a plot or with a blank one is refused`() {
        assertThrows(IllegalStateException::class.java) { SavedStateHandle().requirePlotId() }
        assertThrows(IllegalArgumentException::class.java) { SavedStateHandle(mapOf("plotId" to " ")).requirePlotId() }
    }
}
