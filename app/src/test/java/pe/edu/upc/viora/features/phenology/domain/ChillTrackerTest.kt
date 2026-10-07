package pe.edu.upc.viora.features.phenology.domain

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState

class ChillTrackerTest {

    private fun tracker(
        accumulated: Double,
        threshold: Double = 30.0,
        seasonState: WinterSeasonState = WinterSeasonState.ACCUMULATING,
    ) = ChillTracker(
        plotId = "p-1",
        accumulatedPortions = accumulated,
        thresholdPortions = threshold,
        daysAbove24Celsius = 4,
        seasonState = seasonState,
        projectedCompletionDate = LocalDate.of(2026, 8, 14),
        previousWinterCompletionDate = LocalDate.of(2025, 8, 5),
        ensoRisk = EnsoRiskLevel.NEUTRAL,
        curvePoints = emptyList(),
        syncedAt = Instant.parse("2026-10-07T12:00:00Z"),
    )

    @Test
    fun computesRemainingPortionsCorrectly() {
        val t = tracker(accumulated = 24.0, threshold = 30.0)
        assertEquals(6.0, t.portionsRemaining, 0.001)
    }

    @Test
    fun remainingPortionsNeverNegative() {
        val t = tracker(accumulated = 32.0, threshold = 30.0)
        assertEquals(0.0, t.portionsRemaining, 0.001)
    }

    @Test
    fun computesProgressFractionCorrectly() {
        val t = tracker(accumulated = 15.0, threshold = 30.0)
        assertEquals(0.5f, t.progressFraction, 0.001f)
    }

    @Test
    fun progressFractionCapsAtOne() {
        val t = tracker(accumulated = 35.0, threshold = 30.0)
        assertEquals(1.0f, t.progressFraction, 0.001f)
    }

    @Test
    fun isCompletedWhenThresholdReachedOrStateCompleted() {
        assertFalse(tracker(accumulated = 24.0, seasonState = WinterSeasonState.ACCUMULATING).isCompleted)
        assertTrue(tracker(accumulated = 30.0, seasonState = WinterSeasonState.ACCUMULATING).isCompleted)
        assertTrue(tracker(accumulated = 20.0, seasonState = WinterSeasonState.COMPLETED).isCompleted)
    }
}
