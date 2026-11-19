package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertScheduler
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertSettings

class ObserveCriticalAlertsUseCase @Inject constructor(private val settings: CriticalAlertSettings) {
    operator fun invoke(): Flow<Boolean> = settings.enabled
}

class SetCriticalAlertsUseCase @Inject constructor(private val settings: CriticalAlertSettings) {
    suspend operator fun invoke(enabled: Boolean) = settings.setEnabled(enabled)
}

class UpdateCriticalAlertScheduleUseCase @Inject constructor(
    private val scheduler: CriticalAlertScheduler,
) {
    operator fun invoke(signedIn: Boolean) {
        if (signedIn) scheduler.schedule() else scheduler.cancel()
    }
}
