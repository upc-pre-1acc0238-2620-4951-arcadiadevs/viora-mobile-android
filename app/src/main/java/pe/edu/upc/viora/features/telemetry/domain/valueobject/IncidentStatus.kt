package pe.edu.upc.viora.features.telemetry.domain.valueobject

/**
 * Operational lifecycle status of an agroclimatic incident.
 * [ACTIVE] requires producer attention and mitigation.
 * [SNOOZED] temporarily suppresses push alerts and notifications.
 * [NORMALIZED] marks safe conditions restored after irrigation or cooling.
 */
enum class IncidentStatus {
    ACTIVE,
    SNOOZED,
    NORMALIZED,
    UNKNOWN;

    /** Whether the incident still asks for attention: everything but [NORMALIZED], snoozed ones included. */
    val isActive: Boolean get() = this != NORMALIZED

    companion object {
        fun fromString(value: String): IncidentStatus =
            runCatching { valueOf(value.uppercase()) }.getOrDefault(UNKNOWN)
    }
}
