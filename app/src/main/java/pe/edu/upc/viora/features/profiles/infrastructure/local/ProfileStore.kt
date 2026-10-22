package pe.edu.upc.viora.features.profiles.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId

@Singleton
class ProfileStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val profileFlow: Flow<UserProfile?> = dataStore.data.map { prefs ->
        val userId = prefs[KEY_USER_ID] ?: return@map null
        UserProfile(
            id = ProfileId(prefs[KEY_PROFILE_ID] ?: userId),
            userId = userId,
            fullName = prefs[KEY_FULL_NAME] ?: "",
            country = prefs[KEY_COUNTRY] ?: "",
            phoneNumber = prefs[KEY_PHONE] ?: "",
            email = prefs[KEY_EMAIL],
            role = prefs[KEY_ROLE],
            organization = prefs[KEY_ORGANIZATION],
            licenseActive = prefs[KEY_LICENSE_ACTIVE] ?: true,
        )
    }

    suspend fun getProfile(): UserProfile? {
        val prefs = dataStore.data.first()
        val userId = prefs[KEY_USER_ID] ?: return null
        return UserProfile(
            id = ProfileId(prefs[KEY_PROFILE_ID] ?: userId),
            userId = userId,
            fullName = prefs[KEY_FULL_NAME] ?: "",
            country = prefs[KEY_COUNTRY] ?: "",
            phoneNumber = prefs[KEY_PHONE] ?: "",
            email = prefs[KEY_EMAIL],
            role = prefs[KEY_ROLE],
            organization = prefs[KEY_ORGANIZATION],
            licenseActive = prefs[KEY_LICENSE_ACTIVE] ?: true,
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[KEY_PROFILE_ID] = profile.id.value
            prefs[KEY_USER_ID] = profile.userId
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_COUNTRY] = profile.country
            prefs[KEY_PHONE] = profile.phoneNumber
            profile.email?.let { prefs[KEY_EMAIL] = it }
            profile.role?.let { prefs[KEY_ROLE] = it }
            profile.organization?.let { prefs[KEY_ORGANIZATION] = it }
            prefs[KEY_LICENSE_ACTIVE] = profile.licenseActive
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_PROFILE_ID)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_FULL_NAME)
            prefs.remove(KEY_COUNTRY)
            prefs.remove(KEY_PHONE)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_ORGANIZATION)
            prefs.remove(KEY_LICENSE_ACTIVE)
        }
    }

    val languageFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "es"
    }

    suspend fun saveLanguage(lang: String) {
        dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun getLanguage(): String = dataStore.data.first()[KEY_LANGUAGE] ?: "es"

    private companion object {
        val KEY_PROFILE_ID = stringPreferencesKey("profile_id")
        val KEY_USER_ID = stringPreferencesKey("profile_user_id")
        val KEY_FULL_NAME = stringPreferencesKey("profile_full_name")
        val KEY_COUNTRY = stringPreferencesKey("profile_country")
        val KEY_PHONE = stringPreferencesKey("profile_phone")
        val KEY_EMAIL = stringPreferencesKey("profile_email")
        val KEY_ROLE = stringPreferencesKey("profile_role")
        val KEY_ORGANIZATION = stringPreferencesKey("profile_org")
        val KEY_LICENSE_ACTIVE = booleanPreferencesKey("profile_license_active")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
    }
}
