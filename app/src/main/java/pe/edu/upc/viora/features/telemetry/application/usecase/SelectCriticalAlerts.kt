package pe.edu.upc.viora.features.telemetry.application.usecase

import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

/** Snoozed incidents must not produce notifications until they become active again. */
fun selectCriticalAlerts(incidents: List<AgroclimaticIncident>, notified: Set<String>): List<AgroclimaticIncident> =
    incidents.filter {
        it.status == IncidentStatus.ACTIVE && it.id !in notified &&
            (it.type == IncidentType.FROST_WARNING || it.severity == IncidentSeverity.CRITICAL)
    }.distinctBy { it.id }
