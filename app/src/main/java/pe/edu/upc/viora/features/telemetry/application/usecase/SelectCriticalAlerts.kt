package pe.edu.upc.viora.features.telemetry.application.usecase

import java.time.Instant
import java.time.format.DateTimeParseException
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

/**
 * The critical alerts to notify [now]: frost warnings and critical incidents not notified yet. A snoozed incident
 * waits until its snooze ends; once [AgroclimaticIncident.snoozedUntil] has passed it asks for attention again,
 * as the alerts screen already shows it. A snooze without a readable end keeps waiting.
 */
fun selectCriticalAlerts(incidents: List<AgroclimaticIncident>, notified: Set<String>, now: Instant): List<AgroclimaticIncident> =
    incidents.filter {
        it.asksForAttention(now) && it.id !in notified &&
            (it.type == IncidentType.FROST_WARNING || it.severity == IncidentSeverity.CRITICAL)
    }.distinctBy { it.id }

private fun AgroclimaticIncident.asksForAttention(now: Instant): Boolean = when (status) {
    IncidentStatus.ACTIVE -> true
    IncidentStatus.SNOOZED -> snoozedUntil?.toInstantOrNull()?.let { !it.isAfter(now) } == true
    else -> false
}

private fun String.toInstantOrNull(): Instant? = try {
    Instant.parse(this)
} catch (_: DateTimeParseException) {
    null
}
