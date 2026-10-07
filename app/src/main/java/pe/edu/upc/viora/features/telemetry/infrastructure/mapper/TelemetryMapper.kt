package pe.edu.upc.viora.features.telemetry.infrastructure.mapper

import java.time.Instant
import pe.edu.upc.viora.features.telemetry.domain.entity.HourlyReading
import pe.edu.upc.viora.features.telemetry.infrastructure.local.TelemetryReadingEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.HourlyReadingDto

/** Null when the timestamp is not a valid ISO-8601 instant: the reading is useless without it. */
fun HourlyReadingDto.toEntity(plotId: String): TelemetryReadingEntity? {
    val at = runCatching { Instant.parse(observedAt) }.getOrNull() ?: return null
    return TelemetryReadingEntity(
        id = readingId(plotId, at.toEpochMilli()),
        plotId = plotId,
        observedAtEpochMs = at.toEpochMilli(),
        airTemperatureCelsius = airTemperature,
        relativeHumidityPercent = relativeHumidity,
        soilMoisture30cmPercent = soilMoisture30cm,
        soilMoisture60cmPercent = soilMoisture60cm,
    )
}

fun TelemetryReadingEntity.toDomain(): HourlyReading = HourlyReading(
    observedAt = Instant.ofEpochMilli(observedAtEpochMs),
    airTemperatureCelsius = airTemperatureCelsius,
    relativeHumidityPercent = relativeHumidityPercent,
    soilMoisture30cmPercent = soilMoisture30cmPercent,
    soilMoisture60cmPercent = soilMoisture60cmPercent,
)

private fun readingId(plotId: String, epochMs: Long) = "$plotId|$epochMs"
