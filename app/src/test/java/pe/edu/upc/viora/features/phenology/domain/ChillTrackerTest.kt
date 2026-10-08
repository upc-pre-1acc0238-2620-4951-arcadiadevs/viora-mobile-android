package pe.edu.upc.viora.features.phenology.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.phenology.chillTracker

class ChillTrackerTest {

    @Test
    fun computesRemainingPortions() {
        assertEquals(5.57, chillTracker(accumulated = 24.43).portionsRemaining, 0.001)
    }

    @Test
    fun remainingPortionsNeverNegative() {
        assertEquals(0.0, chillTracker(accumulated = 32.0).portionsRemaining, 0.001)
    }

    @Test
    fun isCompletedOnlyWhenTheBackendGaveACompletionDate() {
        assertFalse(chillTracker(accumulated = 24.43).isCompleted)
        assertTrue(chillTracker(accumulated = 30.1, completionDate = LocalDate.of(2026, 7, 7)).isCompleted)
    }
}
