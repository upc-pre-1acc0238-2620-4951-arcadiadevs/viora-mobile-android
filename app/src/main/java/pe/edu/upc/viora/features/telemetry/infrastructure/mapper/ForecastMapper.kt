package pe.edu.upc.viora.features.telemetry.infrastructure.mapper

import java.time.Instant
import java.time.LocalDate
import kotlin.math.roundToInt
import pe.edu.upc.viora.features.telemetry.domain.entity.ForecastDay
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.telemetry.infrastructure.local.ForecastDayEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.ForecastDayDto

/** Null when the date is not a valid ISO-8601 date. */
fun ForecastDayDto.toEntity(plotId: String, syncedAtEpochMs: Long): ForecastDayEntity? {
    val date = runCatching { LocalDate.parse(forecastDate.take(ISO_DATE_LENGTH)) }.getOrNull() ?: return null
    return ForecastDayEntity(
        id = "$plotId|$date",
        plotId = plotId,
        forecastDate = date.toString(),
        maxTemperatureCelsius = maxTemperature,
        minTemperatureCelsius = minTemperature,
        precipitationProbabilityPercent = precipitationProbability,
        windSpeedKmh = windSpeedKmh,
        syncedAtEpochMs = syncedAtEpochMs,
    )
}

/** Rebuilds the forecast of a plot from its cached days; null when nothing is cached. */
fun List<ForecastDayEntity>.toForecastOrNull(plotId: String): WeatherForecast? {
    if (isEmpty()) return null
    val days = mapNotNull { row ->
        val date = runCatching { LocalDate.parse(row.forecastDate) }.getOrNull() ?: return@mapNotNull null
        ForecastDay(
            date = date,
            maxTemperatureCelsius = row.maxTemperatureCelsius,
            minTemperatureCelsius = row.minTemperatureCelsius,
            precipitationProbabilityPercent = row.precipitationProbabilityPercent.roundToInt().coerceIn(0, 100),
            windSpeedKmh = row.windSpeedKmh,
        )
    }.sortedBy { it.date }
    if (days.isEmpty()) return null
    return WeatherForecast(plotId = plotId, days = days, syncedAt = Instant.ofEpochMilli(maxOf { it.syncedAtEpochMs }))
}

private const val ISO_DATE_LENGTH = 10
