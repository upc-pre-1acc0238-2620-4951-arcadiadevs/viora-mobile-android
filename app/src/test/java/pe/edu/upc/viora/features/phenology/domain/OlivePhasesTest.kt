package pe.edu.upc.viora.features.phenology.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.phenology.chillTracker

/** Which phase of the olive year a plot is in, decided by phenology for every screen that shows it. */
class OlivePhasesTest {

    @Test
    fun `in june the harvest wins over the winter while a plot is still to register`() {
        val today = LocalDate.of(2026, 6, 10)

        assertEquals(OlivePhase.HARVEST, OlivePhases.of(today, PlotPhaseDates(harvestPending = true), null).current)
        assertEquals(OlivePhase.DORMANCY, OlivePhases.of(today, PlotPhaseDates(harvestPending = false), null).current)
    }

    @Test
    fun `a harvest still to register after its season is late, not done`() {
        val today = LocalDate.of(2026, 8, 1)

        assertEquals(PhaseTiming.LATE, OlivePhases.of(today, PlotPhaseDates(harvestPending = true), null).window(OlivePhase.HARVEST).timing)
        assertEquals(PhaseTiming.DONE, OlivePhases.of(today, PlotPhaseDates(), null).window(OlivePhase.HARVEST).timing)
    }

    @Test
    fun `between two phases the next one is current`() {
        val phase = OlivePhases.of(LocalDate.of(2026, 1, 15), PlotPhaseDates(), null)

        assertEquals(OlivePhase.HARVEST, phase.current)
        assertEquals(PhaseTiming.NEXT, phase.window(OlivePhase.HARVEST).timing)
    }

    @Test
    fun `the fruit set ends the day before the plot's thinning window opens`() {
        val dates = PlotPhaseDates(fullBloomOn = LocalDate.of(2026, 10, 1), thinningOpensOn = LocalDate.of(2026, 10, 28))

        val eve = OlivePhases.of(LocalDate.of(2026, 10, 27), dates, null)
        val opening = OlivePhases.of(LocalDate.of(2026, 10, 28), dates, null)

        assertEquals(OlivePhase.FRUIT_SET, eve.current)
        assertEquals(OlivePhase.THINNING, opening.current)
        assertTrue(opening.bloomRecorded)
    }

    @Test
    fun `the dormancy shows the share of chill gathered once it began`() {
        val phase = OlivePhases.of(LocalDate.of(2026, 7, 1), PlotPhaseDates(), chillTracker(accumulated = 15.0, threshold = 30.0))

        assertEquals(0.5f, phase.window(OlivePhase.DORMANCY).progress, 0.001f)
    }

    @Test
    fun `one bloom heat threshold, the same the agroclimatic alerts use`() {
        val phase = OlivePhases.of(LocalDate.of(2026, 9, 20), PlotPhaseDates(), null)

        assertTrue(phase.isBloomHeat(32.0))
        assertFalse(phase.isBloomHeat(31.9))
    }
}
