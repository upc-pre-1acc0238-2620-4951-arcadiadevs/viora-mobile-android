package pe.edu.upc.viora.features.telemetry.domain.valueobject

/**
 * Severity level of an agroclimatic incident.
 * [CRITICAL] indicates immediate crop loss risk (terracotta theme).
 * [WARNING] represents moderate physiological stress (harvest gold theme).
 */
enum class IncidentSeverity {
    CRITICAL,
    WARNING,    
    UNKNOWN;

    companion object {
        fun fromString(value: String): IncidentSeverity =
            runCatching { valueOf(value.uppercase()) }.getOrDefault(UNKNOWN)
    }
}
