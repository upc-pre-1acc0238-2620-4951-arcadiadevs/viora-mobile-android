package pe.edu.upc.viora.features.home.infrastructure.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.home.domain.repository.HomeTourRepository
import pe.edu.upc.viora.features.home.domain.valueobject.HomeTour

/**
 * Keeps the "tour seen" flags in the app's preferences DataStore. They belong to the account on
 * the phone: another account signing in starts without them (see `LocalDataOwner`).
 */
@Singleton
class HomeTourRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : HomeTourRepository {

    override val seenTours: Flow<Set<HomeTour>> = dataStore.data.map { prefs ->
        HomeTour.entries.filterTo(mutableSetOf()) { prefs[it.key] == true }
    }

    override suspend fun markSeen(tour: HomeTour) {
        dataStore.edit { it[tour.key] = true }
    }

    override suspend fun reset() {
        dataStore.edit { prefs -> HomeTour.entries.forEach { prefs.remove(it.key) } }
    }

    private val HomeTour.key
        get() = when (this) {
            // The full tour keeps the key it always had, so producers who saw it are not asked again.
            HomeTour.FULL -> HOME_TOUR_SEEN
            HomeTour.FIRST_STEPS -> FIRST_STEPS_TOUR_SEEN
        }

    internal companion object {
        val HOME_TOUR_SEEN = booleanPreferencesKey("home_tour_seen")
        val FIRST_STEPS_TOUR_SEEN = booleanPreferencesKey("home_tour_first_steps_seen")
    }
}
