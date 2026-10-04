package pe.edu.upc.viora.features.telemetry.domain.entity

/**
 * A virtual sensor node linked to an olive orchard plot (US13, US14).
 *
 * [depthCm] is only present for [SensorType.SONDA_SUELO] (e.g. 30 or 60 cm).
 * [lastReadingAt] is the ISO timestamp of the last simulated reading.
 */
data class SensorNode(
    val id: String,
    val plotId: String,
    val name: String,
    val type: SensorType,
    val depthCm: Int?,
    val status: SensorStatus,
    val lastReadingAt: String?,
    val lastTemperatureCelsius: Double?,
    val lastHumidityPercent: Double?,
)
