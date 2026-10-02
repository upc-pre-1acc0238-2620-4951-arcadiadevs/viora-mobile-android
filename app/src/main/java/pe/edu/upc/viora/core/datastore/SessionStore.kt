package pe.edu.upc.viora.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persists the JWT pair. Until IAM ships (US01-US02, Sprint 3) the backend identifies a
 * fixed producer and no token is ever stored, so the network layer simply sends none.
 *
 * Known gap: values are stored unencrypted in app-private DataStore. Encrypt them with an
 * Android Keystore key when the sign-in flow is implemented (report: AES-256-GCM).
 */
@Singleton
class SessionStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val isSignedIn: Flow<Boolean> = dataStore.data.map { !it[ACCESS_TOKEN].isNullOrBlank() }

    suspend fun accessToken(): String? = dataStore.data.first()[ACCESS_TOKEN]

    suspend fun refreshToken(): String? = dataStore.data.first()[REFRESH_TOKEN]

    suspend fun save(accessToken: String, refreshToken: String) {
        dataStore.edit {
            it[ACCESS_TOKEN] = accessToken
            it[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clear() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(REFRESH_TOKEN)
        }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}
