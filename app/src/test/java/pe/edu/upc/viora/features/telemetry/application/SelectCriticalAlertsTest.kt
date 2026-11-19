package pe.edu.upc.viora.features.telemetry.application

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.application.usecase.selectCriticalAlerts
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

class SelectCriticalAlertsTest {
    private val now = Instant.parse("2026-11-19T12:00:00Z")

    private fun incident(
        id: String,
        type: IncidentType,
        severity: IncidentSeverity,
        status: IncidentStatus = IncidentStatus.ACTIVE,
        snoozedUntil: String? = null,
    ) = AgroclimaticIncident(id, "plot", "Plot", "", type, severity, status, "", "", 0.0, 0.0, "", "", 0, snoozedUntil)

    @Test fun selectsFrostAndCriticalOnceAndExcludesSuppressed() {
        val incidents = listOf(
            incident("frost", IncidentType.FROST_WARNING, IncidentSeverity.WARNING),
            incident("critical", IncidentType.HEAT_WAVE, IncidentSeverity.CRITICAL),
            incident("warning", IncidentType.HEAT_WAVE, IncidentSeverity.WARNING),
            incident("old", IncidentType.FROST_WARNING, IncidentSeverity.CRITICAL),
            incident("snoozed", IncidentType.FROST_WARNING, IncidentSeverity.CRITICAL, IncidentStatus.SNOOZED),
            incident("closed", IncidentType.FROST_WARNING, IncidentSeverity.CRITICAL, IncidentStatus.NORMALIZED),
        )
        assertEquals(listOf("frost", "critical"), selectCriticalAlerts(incidents, setOf("old"), now).map { it.id })
    }

    @Test fun aSnoozeThatAlreadyEndedNotifiesAgainButARunningOneDoesNot() {
        val incidents = listOf(
            incident("ended", IncidentType.HEAT_WAVE, IncidentSeverity.CRITICAL, IncidentStatus.SNOOZED, "2026-10-05T05:46:48Z"),
            incident("running", IncidentType.FROST_WARNING, IncidentSeverity.WARNING, IncidentStatus.SNOOZED, "2026-11-19T18:00:00Z"),
            incident("malformed", IncidentType.FROST_WARNING, IncidentSeverity.CRITICAL, IncidentStatus.SNOOZED, "tomorrow"),
        )
        assertEquals(listOf("ended"), selectCriticalAlerts(incidents, emptySet(), now).map { it.id })
    }
}
