package pe.edu.upc.viora.features.phenology.domain

import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun `default calendar uses usual phase dates and usual source`() {
        val today = LocalDate.of(2026, 7, 1)
        val phase = OlivePhases.of(today, PlotPhaseDates(), null)

        OlivePhase.entries.forEach { p ->
            assertEquals(PhaseDateSource.USUAL, phase.sourceOf(p))
            assertTrue(phase.canAdjustStart(p))
            assertNotNull(phase.startBounds(p))
        }
        assertEquals(LocalDate.of(2026, 6, 1), phase.calendar.dormancyStarts)
        assertEquals(LocalDate.of(2026, 8, 31), phase.calendar.dormancyEnds)
        assertEquals(LocalDate.of(2026, 9, 1), phase.calendar.budbreak)
        assertEquals(LocalDate.of(2026, 10, 5), phase.calendar.fullBloom)
        assertEquals(LocalDate.of(2026, 11, 3), phase.calendar.thinningOpens)
        assertEquals(LocalDate.of(2026, 11, 30), phase.calendar.thinningCloses)
        assertEquals(LocalDate.of(2026, 4, 20), phase.calendar.harvestStarts)
        assertEquals(LocalDate.of(2026, 6, 30), phase.calendar.harvestEnds)
    }

    @Test
    fun `producer adjustments update calendar dates and set source to adjusted`() {
        val adjustments = PhaseAdjustments(
            mapOf(
                OlivePhase.BLOOM to MonthDay.of(7, 15),
                OlivePhase.FRUIT_SET to MonthDay.of(8, 10),
                OlivePhase.HARVEST to MonthDay.of(3, 15),
            ),
        )
        val phase = OlivePhases.of(LocalDate.of(2026, 7, 20), PlotPhaseDates(), null, adjustments)

        assertEquals(OlivePhase.BLOOM, phase.current)
        assertEquals(LocalDate.of(2026, 7, 15), phase.calendar.budbreak)
        assertEquals(LocalDate.of(2026, 7, 14), phase.calendar.dormancyEnds)
        assertEquals(LocalDate.of(2026, 8, 10), phase.calendar.fullBloom)
        assertEquals(LocalDate.of(2026, 3, 15), phase.calendar.harvestStarts)
        val expectedHarvestLength = ChronoUnit.DAYS.between(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 6, 30))
        assertEquals(LocalDate.of(2026, 3, 15).plusDays(expectedHarvestLength), phase.calendar.harvestEnds)

        assertEquals(PhaseDateSource.ADJUSTED, phase.sourceOf(OlivePhase.BLOOM))
        assertEquals(PhaseDateSource.ADJUSTED, phase.sourceOf(OlivePhase.FRUIT_SET))
        assertEquals(PhaseDateSource.ADJUSTED, phase.sourceOf(OlivePhase.HARVEST))
        assertEquals(PhaseDateSource.USUAL, phase.sourceOf(OlivePhase.DORMANCY))
        assertEquals(PhaseDateSource.USUAL, phase.sourceOf(OlivePhase.THINNING))
    }

    @Test
    fun `plot records take precedence over producer adjustments and become recorded source`() {
        val adjustments = PhaseAdjustments(
            mapOf(
                OlivePhase.FRUIT_SET to MonthDay.of(8, 10),
                OlivePhase.THINNING to MonthDay.of(10, 1),
            ),
        )
        val recordedDates = PlotPhaseDates(
            fullBloomOn = LocalDate.of(2026, 10, 2),
            thinningOpensOn = LocalDate.of(2026, 11, 5),
            thinningClosesOn = LocalDate.of(2026, 11, 28),
        )
        val phase = OlivePhases.of(LocalDate.of(2026, 10, 15), recordedDates, null, adjustments)

        assertEquals(LocalDate.of(2026, 10, 2), phase.calendar.fullBloom)
        assertEquals(LocalDate.of(2026, 11, 5), phase.calendar.thinningOpens)
        assertEquals(LocalDate.of(2026, 11, 28), phase.calendar.thinningCloses)

        assertEquals(PhaseDateSource.RECORDED, phase.sourceOf(OlivePhase.FRUIT_SET))
        assertEquals(PhaseDateSource.RECORDED, phase.sourceOf(OlivePhase.THINNING))
        assertFalse(phase.canAdjustStart(OlivePhase.FRUIT_SET))
        assertFalse(phase.canAdjustStart(OlivePhase.THINNING))
    }

    @Test
    fun `start bounds maintain chronological sequence between phases`() {
        val phase = OlivePhases.of(LocalDate.of(2026, 7, 1), PlotPhaseDates(), null)

        val bloomBounds = phase.startBounds(OlivePhase.BLOOM)
        assertEquals(phase.calendar.dormancyStarts.plusDays(1), bloomBounds.earliest)
        assertEquals(phase.calendar.fullBloom.minusDays(1), bloomBounds.latest)
        assertTrue(bloomBounds.contains(LocalDate.of(2026, 8, 15)))
        assertFalse(bloomBounds.contains(phase.calendar.dormancyStarts))
        assertFalse(bloomBounds.contains(phase.calendar.fullBloom))

        val fruitSetBounds = phase.startBounds(OlivePhase.FRUIT_SET)
        assertEquals(phase.calendar.budbreak.plusDays(1), fruitSetBounds.earliest)
        assertEquals(phase.calendar.thinningOpens.minusDays(1), fruitSetBounds.latest)

        val thinningBounds = phase.startBounds(OlivePhase.THINNING)
        assertEquals(phase.calendar.fullBloom.plusDays(1), thinningBounds.earliest)
    }

    @Test
    fun `phase adjustments data class allows modifying and resetting phase starts`() {
        var adjustments = PhaseAdjustments.NONE
        assertTrue(adjustments.isEmpty)
        assertNull(adjustments[OlivePhase.BLOOM])

        adjustments = adjustments.adjusted(OlivePhase.BLOOM, MonthDay.of(7, 15))
        assertFalse(adjustments.isEmpty)
        assertEquals(MonthDay.of(7, 15), adjustments[OlivePhase.BLOOM])

        adjustments = adjustments.reset(OlivePhase.BLOOM)
        assertTrue(adjustments.isEmpty)
        assertNull(adjustments[OlivePhase.BLOOM])
    }
}
