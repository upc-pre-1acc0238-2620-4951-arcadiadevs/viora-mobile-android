package pe.edu.upc.viora.features.profiles.infrastructure

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileAccountDataWiper
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileStore

class ProfileAccountDataWiperTest {

    @Test
    fun `the kept profile leaves the phone with the account`() = runTest {
        val store = ProfileStore(InMemoryPreferences())
        store.saveProfile(UserProfile(ProfileId("p-1"), "u-1", "Teodoro Mamani", "PE", "+51952184337"))

        ProfileAccountDataWiper(store).wipe()

        assertNull(store.getProfile())
    }
}
