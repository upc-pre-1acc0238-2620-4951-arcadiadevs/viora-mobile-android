package pe.edu.upc.viora.features.iam.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import pe.edu.upc.viora.features.iam.domain.repository.OnboardingAnswersRepository
import pe.edu.upc.viora.features.iam.domain.valueobject.OnboardingAnswers

/** The onboarding answers in DataStore. Cleared once the email is verified. */
@Singleton
class OnboardingStore @Inject constructor(private val dataStore: DataStore<Preferences>) : OnboardingAnswersRepository {

    override suspend fun answers(): OnboardingAnswers {
        val prefs = dataStore.data.first()
        return OnboardingAnswers(
            fullName = prefs[FULL_NAME].orEmpty(),
            countryIso = prefs[COUNTRY] ?: OnboardingAnswers.DEFAULT_COUNTRY,
            nationalPhone = prefs[PHONE].orEmpty(),
            cooperativeCode = prefs[COOPERATIVE_CODE],
            hectares = prefs[HECTARES],
            alertsEnabled = prefs[ALERTS],
            locationGranted = prefs[LOCATION],
            termsAccepted = prefs[TERMS_ACCEPTED] == true,
        )
    }

    /** The answers typed so far; the acceptance of the terms is only written by [acceptTerms]. */
    override suspend fun save(answers: OnboardingAnswers) {
        dataStore.edit {
            it[FULL_NAME] = answers.fullName
            it[COUNTRY] = answers.countryIso
            it[PHONE] = answers.nationalPhone
            it.setOrRemove(COOPERATIVE_CODE, answers.cooperativeCode)
            it.setOrRemove(HECTARES, answers.hectares)
            it.setOrRemove(ALERTS, answers.alertsEnabled)
            it.setOrRemove(LOCATION, answers.locationGranted)
        }
    }

    /** One edit of its own, so it never writes back answers read before a save still in progress. */
    override suspend fun acceptTerms() {
        dataStore.edit { it[TERMS_ACCEPTED] = true }
    }

    override suspend fun clear() {
        dataStore.edit { prefs -> ALL_KEYS.forEach { prefs.remove(it) } }
    }

    private fun <T> MutablePreferences.setOrRemove(key: Preferences.Key<T>, value: T?) {
        if (value == null) remove(key) else this[key] = value
    }

    private companion object {
        val FULL_NAME = stringPreferencesKey("onboarding_full_name")
        val COUNTRY = stringPreferencesKey("onboarding_country")
        val PHONE = stringPreferencesKey("onboarding_phone")
        val COOPERATIVE_CODE = stringPreferencesKey("onboarding_cooperative_code")
        val HECTARES = intPreferencesKey("onboarding_hectares")
        val TERMS_ACCEPTED = booleanPreferencesKey("onboarding_terms_accepted")
        val ALERTS = booleanPreferencesKey("onboarding_alerts")
        val LOCATION = booleanPreferencesKey("onboarding_location")
        val ALL_KEYS = listOf(TERMS_ACCEPTED, FULL_NAME, COUNTRY, PHONE, COOPERATIVE_CODE, HECTARES, ALERTS, LOCATION)
    }
}
