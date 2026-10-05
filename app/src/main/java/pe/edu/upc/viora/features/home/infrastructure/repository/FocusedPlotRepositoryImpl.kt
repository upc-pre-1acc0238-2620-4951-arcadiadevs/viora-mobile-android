package pe.edu.upc.viora.features.home.infrastructure.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.home.domain.repository.FocusedPlotRepository

/** Keeps the chosen plot in the app's preferences DataStore. */
@Singleton
class FocusedPlotRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : FocusedPlotRepository {

    override val chosenPlotId: Flow<String?> = dataStore.data.map { it[HOME_FOCUSED_PLOT] }

    override suspend fun choose(plotId: String) {
        dataStore.edit { it[HOME_FOCUSED_PLOT] = plotId }
    }

    private companion object {
        val HOME_FOCUSED_PLOT = stringPreferencesKey("home_focused_plot")
    }
}
