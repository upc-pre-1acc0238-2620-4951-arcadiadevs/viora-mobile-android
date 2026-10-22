package pe.edu.upc.viora.core.database

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.iam.InMemoryPreferences

class LocalDataOwnerTest {

    private val preferences = InMemoryPreferences()
    private var wipes = 0
    private val owner = LocalDataOwner(preferences) { wipes++ }
    private val focusedPlot = stringPreferencesKey("home_focused_plot")

    @Test
    fun `data of an unknown owner is wiped on the first sign-in, since it came from the demo producer`() = runTest {
        owner.claimFor("ana")

        assertEquals(1, wipes)
    }

    @Test
    fun `the same account signing in again keeps its data`() = runTest {
        owner.claimFor("ana")
        owner.claimFor("ana")

        assertEquals(1, wipes)
    }

    @Test
    fun `another account wipes the tables and the plot the previous one had chosen`() = runTest {
        owner.claimFor("ana")
        preferences.edit { it[focusedPlot] = "plot-of-ana" }

        owner.claimFor("bruno")

        assertEquals(2, wipes)
        assertNull(preferences.data.first()[focusedPlot])
    }

    @Test
    fun `another account sees the Home tours again`() = runTest {
        val tourSeen = booleanPreferencesKey("home_tour_seen")
        owner.claimFor("ana")
        preferences.edit { it[tourSeen] = true }

        owner.claimFor("bruno")

        assertNull(preferences.data.first()[tourSeen])
    }

    @Test
    fun `a session from an older version adopts the data instead of losing it`() = runTest {
        owner.adopt("ana")
        owner.claimFor("ana")

        assertEquals(0, wipes)
    }

    @Test
    fun `adopting never replaces a known owner`() = runTest {
        owner.claimFor("ana")
        owner.adopt("bruno")
        owner.claimFor("ana")

        assertEquals(1, wipes)
    }
}
