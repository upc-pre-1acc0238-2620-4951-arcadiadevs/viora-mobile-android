package pe.edu.upc.viora.features.telemetry.application.usecase

import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertNotifier
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertSettings
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository

class CheckCriticalAlertsUseCase @Inject constructor(
    private val repository: IncidentRepository,
    private val settings: CriticalAlertSettings,
    private val notifier: CriticalAlertNotifier,
    private val clock: Clock,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        if (!settings.enabled.first() || !notifier.canNotify()) return AppResult.Success(Unit)
        val result = repository.refresh()
        if (result is AppResult.Failure) return result
        val incidents = repository.observeIncidents().first()
        settings.retainNotified(incidents.map { it.id }.toSet())
        val alerts = selectCriticalAlerts(incidents, settings.notifiedIds(), clock.instant())
        for (alert in alerts) {
            if (!settings.enabled.first() || !notifier.canNotify()) break
            // Persist before posting: a process crash can miss an alert, but cannot post it twice.
            settings.markNotified(alert.id)
            notifier.show(alert)
        }
        return AppResult.Success(Unit)
    }
}
