package pe.edu.upc.viora.features.telemetry.infrastructure.mapper

import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorStatus
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.infrastructure.local.SensorNodeEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.CalibrateSensorNodeRequestDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.LinkSensorNodeRequestDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.SensorNodeDto

fun SensorNodeDto.toEntity(): SensorNodeEntity = SensorNodeEntity(
    id = id,
    plotId = plotId,
    name = name,
    type = type,
    depthCm = depthCm,
    status = status,
    lastReadingAt = lastReadingAt,
    lastTemperatureCelsius = lastTemperatureCelsius,
    lastHumidityPercent = lastHumidityPercent,
)

fun SensorNodeEntity.toDomainOrNull(): SensorNode? {
    val nodeType = sensorTypeOf(type) ?: return null
    val nodeStatus = runCatching { SensorStatus.valueOf(status) }.getOrNull() ?: return null
    return SensorNode(
        id = id,
        plotId = plotId,
        name = name,
        type = nodeType,
        depthCm = depthCm,
        status = nodeStatus,
        lastReadingAt = lastReadingAt,
        lastTemperatureCelsius = lastTemperatureCelsius,
        lastHumidityPercent = lastHumidityPercent,
    )
}

fun NewSensorNode.toRequestDto(): LinkSensorNodeRequestDto = LinkSensorNodeRequestDto(
    name = name,
    type = type.apiName(),
    depthCm = depthCm,
)

fun SensorNode.toCalibrationRequestDto(): CalibrateSensorNodeRequestDto = CalibrateSensorNodeRequestDto(
    name = name,
    depthCm = depthCm ?: 30,
)

/**
 * The backend names the node types in English (`MICROCLIMATE`, `SOIL_PROBE`); rows cached by older versions
 * of the app kept the app's names, so both are read.
 */
private fun sensorTypeOf(raw: String): SensorType? = when (raw) {
    "MICROCLIMATE", "MICROCLIMA" -> SensorType.MICROCLIMA
    "SOIL_PROBE", "SONDA_SUELO" -> SensorType.SONDA_SUELO
    else -> null
}

/** The name of the type the backend accepts when a node is linked. */
private fun SensorType.apiName(): String = when (this) {
    SensorType.MICROCLIMA -> "MICROCLIMATE"
    SensorType.SONDA_SUELO -> "SOIL_PROBE"
}
