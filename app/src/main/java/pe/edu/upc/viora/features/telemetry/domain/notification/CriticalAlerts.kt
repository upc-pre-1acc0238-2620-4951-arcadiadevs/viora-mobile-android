package pe.edu.upc.viora.features.telemetry.domain.notification

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident

interface CriticalAlertSettings {
    val enabled: Flow<Boolean>
    suspend fun setEnabled(enabled: Boolean)
    suspend fun notifiedIds(): Set<String>
    suspend fun retainNotified(ids: Set<String>)
    suspend fun markNotified(id: String)
    suspend fun clearNotified()
}

interface CriticalAlertNotifier {
    fun canNotify(): Boolean
    fun show(incident: AgroclimaticIncident)
}

interface CriticalAlertScheduler {
    fun schedule()
    fun cancel()
}
