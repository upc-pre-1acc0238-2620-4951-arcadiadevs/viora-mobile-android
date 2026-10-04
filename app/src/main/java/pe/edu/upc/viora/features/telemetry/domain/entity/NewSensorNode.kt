package pe.edu.upc.viora.features.telemetry.domain.entity

/** Validated data needed to link a new virtual sensor node (US13). */
data class NewSensorNode(
    val plotId: String,
    val name: String,
    val type: SensorType,
    val depthCm: Int?,
)
