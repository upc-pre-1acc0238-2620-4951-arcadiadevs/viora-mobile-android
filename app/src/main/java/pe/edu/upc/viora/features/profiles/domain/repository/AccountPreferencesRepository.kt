package pe.edu.upc.viora.features.profiles.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Preferences of whoever uses the phone (Mi cuenta › Preferencias). They stay on the phone when the
 * account changes, like the language.
 */
interface AccountPreferencesRepository {

    /** Whether Viora's fixed texts are written letter by letter; on by default. */
    val animatedVoice: Flow<Boolean>

    suspend fun setAnimatedVoice(enabled: Boolean)
}
