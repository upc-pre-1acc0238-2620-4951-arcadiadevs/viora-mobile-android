package pe.edu.upc.viora.features.telemetry.domain.valueobject

enum class IncidentType {
    HEAT_WAVE,
    HYDRIC_STRESS,
    FROST_WARNING,
    UNKNOWN;

    companion object {
        fun fromString(value: String): IncidentType =
            runCatching { valueOf(value.uppercase()) }.getOrDefault(UNKNOWN)
    }
}
