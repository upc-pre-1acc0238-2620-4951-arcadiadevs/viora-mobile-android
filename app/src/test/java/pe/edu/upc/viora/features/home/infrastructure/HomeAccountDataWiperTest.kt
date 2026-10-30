package pe.edu.upc.viora.features.home.infrastructure

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.home.infrastructure.local.HomeAccountDataWiper
import pe.edu.upc.viora.features.iam.InMemoryPreferences

class HomeAccountDataWiperTest {

    private val preferences = InMemoryPreferences()
    private val focusedPlot = stringPreferencesKey("home_focused_plot")
    private val tourSeen = booleanPreferencesKey("home_tour_seen")
    private val firstStepsSeen = booleanPreferencesKey("home_tour_first_steps_seen")
    private val onboardingName = stringPreferencesKey("onboarding_full_name")

    @Test
    fun `the next account sees the Home tours again and chooses its own plot`() = runTest {
        preferences.edit {
            it[focusedPlot] = "plot-of-ana"
            it[tourSeen] = true
            it[firstStepsSeen] = true
        }

        HomeAccountDataWiper(preferences).wipe()

        val prefs = preferences.data.first()
        assertNull(prefs[focusedPlot])
        assertNull(prefs[tourSeen])
        assertNull(prefs[firstStepsSeen])
    }

    @Test
    fun `preferences of other contexts stay`() = runTest {
        preferences.edit { it[onboardingName] = "Teodoro" }

        HomeAccountDataWiper(preferences).wipe()

        assertEquals("Teodoro", preferences.data.first()[onboardingName])
    }
}
