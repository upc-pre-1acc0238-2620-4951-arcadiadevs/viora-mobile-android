package pe.edu.upc.viora.features.telemetry.domain.valueobject

/**
 * Types of agroclimatic incidents originating within the Telemetry bounded context.
 * Scoped to microclimatic and soil anomalies: heat waves, hydric stress, and frost risk.
 */
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
