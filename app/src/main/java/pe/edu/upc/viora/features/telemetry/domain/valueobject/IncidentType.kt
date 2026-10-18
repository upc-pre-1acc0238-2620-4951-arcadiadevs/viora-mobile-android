package pe.edu.upc.viora.features.telemetry.domain.valueobject

/**
 * Types of agroclimatic incidents originating within the Telemetry bounded context.
 * Scoped to microclimatic and soil anomalies: heat waves, hydric stress, frost risk and the warm winter spell that
 * halts the chill count (US23, raised from the plot's daily maxima, not from an El Niño index).
 */
enum class IncidentType {
    HEAT_WAVE,
    HYDRIC_STRESS,
    FROST_WARNING,
    WARM_WINTER,
    UNKNOWN;

    companion object {
        fun fromString(value: String): IncidentType =
            runCatching { valueOf(value.uppercase()) }.getOrDefault(UNKNOWN)
    }
}
