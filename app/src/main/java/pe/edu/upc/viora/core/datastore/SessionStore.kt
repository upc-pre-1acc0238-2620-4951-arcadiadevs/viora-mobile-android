package pe.edu.upc.viora.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** The saved session: the JWT pair, when the access token expires and whose account it is. */
data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    /** Epoch millis; 0 for sessions saved before the expiry was stored, so they renew right away. */
    val expiresAtMillis: Long,
    val userId: String?,
)

/**
 * Persists the session of the signed-in producer.
 *
 * Known gap: values are stored unencrypted in app-private DataStore. Encrypt them with an
 * Android Keystore key (report: AES-256-GCM).
 */
@Singleton
class SessionStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val isSignedIn: Flow<Boolean> = dataStore.data.map { !it[ACCESS_TOKEN].isNullOrBlank() }

    suspend fun accessToken(): String? = dataStore.data.first()[ACCESS_TOKEN]

    suspend fun refreshToken(): String? = dataStore.data.first()[REFRESH_TOKEN]

    suspend fun session(): StoredSession? {
        val prefs = dataStore.data.first()
        val access = prefs[ACCESS_TOKEN]?.takeIf { it.isNotBlank() } ?: return null
        val refresh = prefs[REFRESH_TOKEN]?.takeIf { it.isNotBlank() } ?: return null
        return StoredSession(access, refresh, prefs[EXPIRES_AT] ?: 0L, prefs[USER_ID])
    }

    suspend fun save(session: StoredSession) {
        dataStore.edit {
            it[ACCESS_TOKEN] = session.accessToken
            it[REFRESH_TOKEN] = session.refreshToken
            it[EXPIRES_AT] = session.expiresAtMillis
            if (session.userId != null) it[USER_ID] = session.userId else it.remove(USER_ID)
        }
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(REFRESH_TOKEN)
            it.remove(EXPIRES_AT)
            it.remove(USER_ID)
        }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("access_token_expires_at")
        val USER_ID = stringPreferencesKey("user_id")
    }
}
