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

/** Keeps the "tour seen" flag in the app's preferences DataStore. */
@Singleton
class HomeTourRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : HomeTourRepository {

    override val hasSeenTour: Flow<Boolean> = dataStore.data.map { it[HOME_TOUR_SEEN] ?: false }

    override suspend fun markSeen() {
        dataStore.edit { it[HOME_TOUR_SEEN] = true }
    }

    override suspend fun reset() {
        dataStore.edit { it.remove(HOME_TOUR_SEEN) }
    }

    private companion object {
        val HOME_TOUR_SEEN = booleanPreferencesKey("home_tour_seen")
    }
}
