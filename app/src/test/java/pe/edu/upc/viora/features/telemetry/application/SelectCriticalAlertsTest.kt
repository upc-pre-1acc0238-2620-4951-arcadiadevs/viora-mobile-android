package pe.edu.upc.viora.features.telemetry.application

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.application.usecase.selectCriticalAlerts
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

class SelectCriticalAlertsTest {
    private fun incident(id: String, type: IncidentType, severity: IncidentSeverity, status: IncidentStatus = IncidentStatus.ACTIVE) =
        AgroclimaticIncident(id, "plot", "Plot", "", type, severity, status, "", "", 0.0, 0.0, "", "", 0)

    @Test fun selectsFrostAndCriticalOnceAndExcludesSuppressed() {
        val incidents = listOf(
            incident("frost", IncidentType.FROST_WARNING, IncidentSeverity.WARNING),
            incident("critical", IncidentType.HEAT_WAVE, IncidentSeverity.CRITICAL),
            incident("warning", IncidentType.HEAT_WAVE, IncidentSeverity.WARNING),
            incident("old", IncidentType.FROST_WARNING, IncidentSeverity.CRITICAL),
            incident("snoozed", IncidentType.FROST_WARNING, IncidentSeverity.CRITICAL, IncidentStatus.SNOOZED),
            incident("closed", IncidentType.FROST_WARNING, IncidentSeverity.CRITICAL, IncidentStatus.NORMALIZED),
        )
        assertEquals(listOf("frost", "critical"), selectCriticalAlerts(incidents, setOf("old")).map { it.id })
    }

}
