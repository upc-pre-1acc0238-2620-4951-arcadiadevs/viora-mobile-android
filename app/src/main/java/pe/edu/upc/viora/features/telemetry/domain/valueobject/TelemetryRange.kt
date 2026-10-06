package pe.edu.upc.viora.features.telemetry.domain.valueobject

/** Time window the producer picks to read the hourly series of a plot (US17, scenario 1). */
enum class TelemetryRange(val hours: Long) {
    LAST_24_HOURS(24),
    LAST_7_DAYS(24 * 7),
    LAST_30_DAYS(24 * 30),
}
