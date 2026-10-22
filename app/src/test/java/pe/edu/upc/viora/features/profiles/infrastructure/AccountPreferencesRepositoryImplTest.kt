package pe.edu.upc.viora.features.profiles.infrastructure

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.profiles.infrastructure.repository.AccountPreferencesRepositoryImpl

class AccountPreferencesRepositoryImplTest {

    private val repository = AccountPreferencesRepositoryImpl(InMemoryPreferences())

    @Test
    fun `the voice is animated until the producer turns it off`() = runTest {
        assertTrue(repository.animatedVoice.first())

        repository.setAnimatedVoice(false)
        assertFalse(repository.animatedVoice.first())

        repository.setAnimatedVoice(true)
        assertTrue(repository.animatedVoice.first())
    }
}
