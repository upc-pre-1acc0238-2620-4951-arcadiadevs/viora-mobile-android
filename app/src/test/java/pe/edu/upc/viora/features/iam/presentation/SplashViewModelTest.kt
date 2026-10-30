package pe.edu.upc.viora.features.iam.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.database.LocalDataOwner
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.datastore.StoredSession
import pe.edu.upc.viora.core.datastore.TokenCipher
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.iam.presentation.viewmodel.SplashDestination
import pe.edu.upc.viora.features.iam.presentation.viewmodel.SplashViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val store = SessionStore(InMemoryPreferences(), TokenCipher.PLAIN)
    private var wipes = 0
    private val owner = LocalDataOwner(InMemoryPreferences()) { wipes++ }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun SplashViewModel.decided() = destination.filterNotNull().first()

    @Test
    fun `without a saved session the splash goes to the welcome`() = runTest {
        assertEquals(SplashDestination.WELCOME, SplashViewModel(store, owner).decided())
    }

    @Test
    fun `a saved session opens the home`() = runTest {
        store.save(StoredSession("access", "refresh", expiresAtMillis = 0, userId = "ana"))

        assertEquals(SplashDestination.HOME, SplashViewModel(store, owner).decided())
    }

    @Test
    fun `a session saved by an older version keeps the data on the phone as its own`() = runTest {
        store.save(StoredSession("access", "refresh", expiresAtMillis = 0, userId = "ana"))

        SplashViewModel(store, owner).decided()
        owner.claimFor("ana")

        assertEquals(0, wipes)
    }

    @Test
    fun `nothing is adopted without a session, so the next account starts clean`() = runTest {
        SplashViewModel(store, owner).decided()
        owner.claimFor("ana")

        assertEquals(1, wipes)
    }
}
