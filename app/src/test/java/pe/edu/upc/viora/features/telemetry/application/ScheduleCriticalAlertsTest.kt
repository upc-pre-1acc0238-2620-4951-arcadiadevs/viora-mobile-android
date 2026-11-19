package pe.edu.upc.viora.features.telemetry.application

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.application.usecase.UpdateCriticalAlertScheduleUseCase
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertScheduler

class ScheduleCriticalAlertsTest {
    @Test fun schedulesOnlyWhenSignedIn() {
        var calls = 0
        var cancellations = 0
        val scheduler = object : CriticalAlertScheduler { override fun schedule() { calls++ }; override fun cancel() { cancellations++ } }
        val useCase = UpdateCriticalAlertScheduleUseCase(scheduler)
        useCase(false)
        assertEquals(0, calls)
        assertEquals(1, cancellations)
        useCase(true)
        assertEquals(1, calls)
        assertEquals(1, cancellations)
    }
}
