package pe.edu.upc.viora.features.home.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.home.infrastructure.repository.FocusedPlotRepositoryImpl
import pe.edu.upc.viora.features.home.infrastructure.repository.HomeTourRepositoryImpl

/** The Home preferences of one account: the plot it chose and the tours it saw, so the next account sees them again. */
class HomeAccountDataWiper @Inject constructor(private val dataStore: DataStore<Preferences>) : AccountDataWiper {
    override suspend fun wipe() {
        dataStore.edit {
            it.remove(FocusedPlotRepositoryImpl.HOME_FOCUSED_PLOT)
            it.remove(HomeTourRepositoryImpl.HOME_TOUR_SEEN)
            it.remove(HomeTourRepositoryImpl.FIRST_STEPS_TOUR_SEEN)
        }
    }
}
