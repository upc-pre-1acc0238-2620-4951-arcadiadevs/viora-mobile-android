package pe.edu.upc.viora.features.telemetry.domain.entity

import java.time.Instant
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric

/**
 * One hourly measurement of the plot's virtual nodes (`HourlyTelemetryReading`, report table 70).
 * Every value is optional because a node can be paused or may not measure that magnitude.
 *
 * Soil moisture is volumetric water content in percent; the 30 cm probe is the one that drives
 * the irrigation advice.
 */
data class HourlyReading(
    val observedAt: Instant,
    val airTemperatureCelsius: Double?,
    val relativeHumidityPercent: Double?,
    val soilMoisture30cmPercent: Double?,
    val soilMoisture60cmPercent: Double?,
) {
    fun valueOf(metric: TelemetryMetric): Double? = when (metric) {
        TelemetryMetric.TEMPERATURE -> airTemperatureCelsius
        TelemetryMetric.AIR_HUMIDITY -> relativeHumidityPercent
        TelemetryMetric.SOIL_MOISTURE -> soilMoisture30cmPercent
    }
}
