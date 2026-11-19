package pe.edu.upc.viora.features.phenology.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.phenology.infrastructure.repository.PhaseAdjustmentsRepositoryImpl

/** The phase dates a producer corrected belong to their plots, so the next account does not inherit them. */
class PhaseAdjustmentsWiper @Inject constructor(private val dataStore: DataStore<Preferences>) : AccountDataWiper {
    override suspend fun wipe() {
        dataStore.edit { it.remove(PhaseAdjustmentsRepositoryImpl.PHASE_ADJUSTMENTS) }
    }
}
