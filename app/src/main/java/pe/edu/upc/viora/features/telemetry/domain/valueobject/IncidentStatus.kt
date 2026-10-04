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

    companion object {
        fun fromString(value: String): IncidentStatus =
            runCatching { valueOf(value.uppercase()) }.getOrDefault(UNKNOWN)
    }
}
