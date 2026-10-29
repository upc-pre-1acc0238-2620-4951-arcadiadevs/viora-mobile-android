package pe.edu.upc.viora.features.subscription.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.subscription.infrastructure.repository.PlanExtensionRepositoryImpl

/** The plan extension an account went to pay and has not confirmed yet belongs only to that account. */
class PlanExtensionAccountDataWiper @Inject constructor(private val dataStore: DataStore<Preferences>) : AccountDataWiper {
    override suspend fun wipe() {
        dataStore.edit {
            it.remove(PlanExtensionRepositoryImpl.PENDING_SUBSCRIPTION)
            it.remove(PlanExtensionRepositoryImpl.PENDING_EXTENSION)
        }
    }
}
