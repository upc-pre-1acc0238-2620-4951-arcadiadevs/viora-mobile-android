package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * DTO for `/api/v1/plots/{plotId}/iot-devices`.
 */
@Serializable
data class SensorNodeDto(
    val id: String,
    val plotId: String,
    val name: String,
    val type: String,
    val depthCm: Int? = null,
    val status: String,
    val lastReadingAt: String? = null,
    val lastTemperatureCelsius: Double? = null,
    val lastHumidityPercent: Double? = null,
)
