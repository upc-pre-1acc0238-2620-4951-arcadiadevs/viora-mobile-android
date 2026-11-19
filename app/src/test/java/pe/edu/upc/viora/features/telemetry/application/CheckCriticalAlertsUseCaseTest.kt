package pe.edu.upc.viora.features.telemetry.application

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.application.usecase.CheckCriticalAlertsUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertNotifier
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertSettings
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

class CheckCriticalAlertsUseCaseTest {
    private val incident = AgroclimaticIncident("id", "plot", "Plot", "", IncidentType.FROST_WARNING, IncidentSeverity.WARNING, IncidentStatus.ACTIVE, "", "", 0.0, 0.0, "", "", 0)
    private val settings = object : CriticalAlertSettings {
        override val enabled = MutableStateFlow(true)
        val ids = mutableSetOf<String>()
        override suspend fun setEnabled(enabled: Boolean) { this.enabled.value = enabled }
        override suspend fun notifiedIds(): Set<String> = ids.toSet()
        override suspend fun markNotified(id: String) { ids.add(id) }
        override suspend fun retainNotified(ids: Set<String>) { this.ids.retainAll(ids) }
        override suspend fun clearNotified() { ids.clear() }
    }
    private val notifier = object : CriticalAlertNotifier {
        var available = true
        val shown = mutableListOf<String>()
        override fun canNotify() = available
        override fun show(incident: AgroclimaticIncident) { shown.add(incident.id) }
    }
    private val repository = object : IncidentRepository {
        var refreshes = 0
        override fun observeIncidents(plotId: String?) = flowOf(listOf(incident))
        override suspend fun refresh(plotId: String?, status: String?, severity: String?): AppResult<AlertsSummary> {
            refreshes++
            return AppResult.Success(AlertsSummary())
        }
        override suspend fun getIncidentDetail(incidentId: String) = AppResult.Failure(AppError.Offline)
        override suspend fun postponeIncident(incidentId: String, durationHours: Int) = AppResult.Failure(AppError.Offline)
        override suspend fun completeMitigationStep(incidentId: String, stepId: String) = AppResult.Failure(AppError.Offline)
        override suspend fun getSummary() = AppResult.Failure(AppError.Offline)
    }
    private val check = CheckCriticalAlertsUseCase(repository, settings, notifier, Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC))

    @Test fun preferenceOffDoesNotRefreshOrNotify() = runTest {
        settings.setEnabled(false)
        check()
        assertEquals(0, repository.refreshes)
        assertEquals(emptyList<String>(), notifier.shown)
    }

    @Test fun notifiedIncidentIsNotPostedAgain() = runTest {
        check()
        check()
        assertEquals(listOf("id"), notifier.shown)
        assertEquals(setOf("id"), settings.notifiedIds())
    }

    @Test fun unavailablePermissionDoesNotConsumeIncident() = runTest {
        notifier.available = false
        check()
        assertEquals(emptySet<String>(), settings.notifiedIds())
        notifier.available = true
        check()
        assertEquals(listOf("id"), notifier.shown)
    }

    @Test fun prunesIdsAbsentFromCurrentIncidentsButRetainsCurrentAndNew() = runTest {
        settings.ids.addAll(setOf("old", "id"))
        check()
        assertEquals(setOf("id"), settings.notifiedIds())
        assertEquals(emptyList<String>(), notifier.shown)
        settings.ids.clear()
        settings.ids.add("old")
        check()
        assertEquals(setOf("id"), settings.notifiedIds())
        assertEquals(listOf("id"), notifier.shown)
    }
}
