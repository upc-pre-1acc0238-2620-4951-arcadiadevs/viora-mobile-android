package pe.edu.upc.viora.features.telemetry.infrastructure.mapper

import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorStatus
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.infrastructure.local.SensorNodeEntity
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
    val nodeType = runCatching { SensorType.valueOf(type) }.getOrNull() ?: return null
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
    type = type.name,
    depthCm = depthCm,
)
