package pe.edu.upc.viora.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** The tokens never reach the disk in clear, and a session saved before the encryption still opens. */
class SessionStoreCipherTest {

    @get:Rule
    val folder = TemporaryFolder()

    /** Marks what it encrypts, so the test can see what reaches the disk; "broken" cannot be decrypted. */
    private val cipher = object : TokenCipher {
        override fun encrypt(plain: String) = "v1:" + plain.reversed()
        override fun decrypt(stored: String) = when {
            stored == "v1:broken" -> null
            stored.startsWith("v1:") -> stored.removePrefix("v1:").reversed()
            else -> stored
        }
    }

    private fun dataStore(name: String) = PreferenceDataStoreFactory.create { File(folder.root, "$name.preferences_pb") }

    @Test
    fun `stores the tokens encrypted and reads them back`() = runTest {
        val dataStore = dataStore("encrypted")
        val store = SessionStore(dataStore, cipher)

        store.save(StoredSession("access-1", "refresh-1", 10L, "user-1"))

        assertEquals("v1:1-hserfer", dataStore.data.first()[stringPreferencesKey("refresh_token")])
        assertEquals(StoredSession("access-1", "refresh-1", 10L, "user-1"), store.session())
    }

    @Test
    fun `opens a session saved before the encryption, and treats an undecryptable one as signed out`() = runTest {
        val legacy = dataStore("legacy")
        legacy.edit {
            it[stringPreferencesKey("access_token")] = "old-access"
            it[stringPreferencesKey("refresh_token")] = "old-refresh"
        }
        assertEquals("old-refresh", SessionStore(legacy, cipher).session()?.refreshToken)

        val broken = dataStore("broken")
        broken.edit {
            it[stringPreferencesKey("access_token")] = "v1:broken"
            it[stringPreferencesKey("refresh_token")] = "v1:broken"
        }
        assertNull(SessionStore(broken, cipher).session())
    }
}
