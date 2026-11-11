package pe.edu.upc.viora.features.plotmanagement.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.plotmanagement.domain.repository.SectorPromptRepository

/** Keeps the P06b flag in the app's preferences DataStore; it belongs to the account on the phone. */
@Singleton
class SectorPromptRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SectorPromptRepository {

    override val answered: Flow<Boolean> = dataStore.data.map { it[SECTOR_PROMPT_ANSWERED] == true }

    override suspend fun markAnswered() {
        dataStore.edit { it[SECTOR_PROMPT_ANSWERED] = true }
    }

    internal companion object {
        val SECTOR_PROMPT_ANSWERED = booleanPreferencesKey("plot_sector_prompt_answered")
    }
}

/** Another account on the phone is asked P06b again. */
class SectorPromptWiper @Inject constructor(private val dataStore: DataStore<Preferences>) : AccountDataWiper {
    override suspend fun wipe() {
        dataStore.edit { it.remove(SectorPromptRepositoryImpl.SECTOR_PROMPT_ANSWERED) }
    }
}
