package pe.edu.upc.viora.features.profiles.infrastructure.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.profiles.domain.repository.AccountPreferencesRepository

/** Keeps the phone's preferences in the app's preferences DataStore (not wiped on account change). */
@Singleton
class AccountPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : AccountPreferencesRepository {

    override val animatedVoice: Flow<Boolean> = dataStore.data.map { it[ANIMATED_VOICE] ?: true }

    override suspend fun setAnimatedVoice(enabled: Boolean) {
        dataStore.edit { it[ANIMATED_VOICE] = enabled }
    }

    private companion object {
        val ANIMATED_VOICE = booleanPreferencesKey("viora_voice_animated")
    }
}
