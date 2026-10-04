package pe.edu.upc.viora.features.telemetry.domain.valueobject

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
