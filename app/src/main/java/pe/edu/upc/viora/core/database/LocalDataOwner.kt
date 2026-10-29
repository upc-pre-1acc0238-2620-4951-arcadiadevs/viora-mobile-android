package pe.edu.upc.viora.core.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Wipes part of what the phone keeps for an account. Each bounded context adds its own with `@IntoSet`:
 * its tables, files and preferences. The app provides the one [LocalDataOwner] runs, which calls them all.
 */
fun interface AccountDataWiper {
    suspend fun wipe()
}

/**
 * Remembers whose data the phone holds. When another account opens a session, the data of the
 * previous one is wiped first, so a new account never shows someone else's plots or alerts. The
 * onboarding answers stay: they belong to whoever is signing up on this phone.
 */
@Singleton
class LocalDataOwner @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val wiper: AccountDataWiper,
) {

    /**
     * Called right before saving the session that a sign-in or a verified sign-up opened. Data of an
     * unknown owner is wiped too: it comes from the demo producer the app showed before having accounts.
     */
    suspend fun claimFor(userId: String?) {
        val owner = dataStore.data.first()[OWNER]
        if (owner != null && owner == userId) return
        wiper.wipe()
        dataStore.edit { prefs -> if (userId != null) prefs[OWNER] = userId else prefs.remove(OWNER) }
    }

    /**
     * Called when the producer signs out: everything of the account leaves the phone, so whoever uses it next sees
     * nothing of the farm. The onboarding answers stay, like on [claimFor].
     */
    suspend fun release() = claimFor(null)

    /**
     * A session saved before the owner was recorded adopts the data on the phone instead of losing
     * it, so records still waiting to sync survive the update.
     */
    suspend fun adopt(userId: String?) {
        if (userId == null) return
        dataStore.edit { prefs -> if (prefs[OWNER] == null) prefs[OWNER] = userId }
    }

    private companion object {
        val OWNER = stringPreferencesKey("local_data_owner")
    }
}
