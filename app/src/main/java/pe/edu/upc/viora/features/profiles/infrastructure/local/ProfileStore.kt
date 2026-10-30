package pe.edu.upc.viora.features.profiles.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId

/** Keeps the signed-in producer's profile in the app's preferences, so it shows offline. */
@Singleton
class ProfileStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val profileFlow: Flow<UserProfile?> = dataStore.data.map(::read)

    suspend fun getProfile(): UserProfile? = read(dataStore.data.first())

    suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[KEY_PROFILE_ID] = profile.id.value
            prefs[KEY_USER_ID] = profile.userId
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_COUNTRY] = profile.country
            prefs[KEY_PHONE] = profile.phoneNumber
            if (profile.email != null) prefs[KEY_EMAIL] = profile.email else prefs.remove(KEY_EMAIL)
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs -> ALL_KEYS.forEach { prefs.remove(it) } }
    }

    private fun read(prefs: Preferences): UserProfile? {
        val userId = prefs[KEY_USER_ID]?.takeIf(String::isNotBlank) ?: return null
        return UserProfile(
            id = ProfileId(prefs[KEY_PROFILE_ID]?.takeIf(String::isNotBlank) ?: userId),
            userId = userId,
            fullName = prefs[KEY_FULL_NAME].orEmpty(),
            country = prefs[KEY_COUNTRY].orEmpty(),
            phoneNumber = prefs[KEY_PHONE].orEmpty(),
            email = prefs[KEY_EMAIL],
        )
    }

    private companion object {
        val KEY_PROFILE_ID = stringPreferencesKey("profile_id")
        val KEY_USER_ID = stringPreferencesKey("profile_user_id")
        val KEY_FULL_NAME = stringPreferencesKey("profile_full_name")
        val KEY_COUNTRY = stringPreferencesKey("profile_country")
        val KEY_PHONE = stringPreferencesKey("profile_phone")
        val KEY_EMAIL = stringPreferencesKey("profile_email")
        val ALL_KEYS = listOf(KEY_PROFILE_ID, KEY_USER_ID, KEY_FULL_NAME, KEY_COUNTRY, KEY_PHONE, KEY_EMAIL)
    }
}
