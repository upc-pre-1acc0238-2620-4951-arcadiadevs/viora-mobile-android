package pe.edu.upc.viora.features.telemetry.domain.valueobject

enum class IncidentSeverity {
    CRITICAL,
    WARNING,
    UNKNOWN;

    companion object {
        fun fromString(value: String): IncidentSeverity =
            runCatching { valueOf(value.uppercase()) }.getOrDefault(UNKNOWN)
    }
}
