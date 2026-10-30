package pe.edu.upc.viora.core.database

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.iam.InMemoryPreferences

class LocalDataOwnerTest {

    private val preferences = InMemoryPreferences()
    private var wipes = 0
    private val owner = LocalDataOwner(preferences) { wipes++ }

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
    fun `another account wipes the data of the previous one`() = runTest {
        owner.claimFor("ana")

        owner.claimFor("bruno")

        assertEquals(2, wipes)
    }

    @Test
    fun `signing out wipes the data and forgets the owner`() = runTest {
        owner.claimFor("ana")

        owner.release()
        owner.claimFor("ana")

        assertEquals(3, wipes)
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
