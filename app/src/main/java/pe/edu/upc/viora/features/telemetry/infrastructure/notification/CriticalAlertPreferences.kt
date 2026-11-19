package pe.edu.upc.viora.features.telemetry.infrastructure.notification

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertSettings

@Singleton
class CriticalAlertPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) : CriticalAlertSettings {
    override val enabled: Flow<Boolean> = dataStore.data.map { it[ENABLED] ?: true }
    override suspend fun setEnabled(enabled: Boolean) { dataStore.edit { it[ENABLED] = enabled } }
    override suspend fun notifiedIds(): Set<String> = dataStore.data.first()[NOTIFIED] ?: emptySet()
    override suspend fun retainNotified(ids: Set<String>) { dataStore.edit { it[NOTIFIED] = (it[NOTIFIED] ?: emptySet()) intersect ids } }
    override suspend fun markNotified(id: String) { dataStore.edit { it[NOTIFIED] = (it[NOTIFIED] ?: emptySet()) + id } }
    override suspend fun clearNotified() { dataStore.edit { it.remove(NOTIFIED) } }
    private companion object {
        val ENABLED = booleanPreferencesKey("critical_alerts_enabled")
        val NOTIFIED = stringSetPreferencesKey("critical_alerts_notified")
    }
}
