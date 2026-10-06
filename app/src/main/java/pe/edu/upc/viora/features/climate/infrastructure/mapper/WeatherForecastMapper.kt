package pe.edu.upc.viora.features.climate.infrastructure.mapper

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import pe.edu.upc.viora.features.climate.domain.entity.DayForecast
import pe.edu.upc.viora.features.climate.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.climate.infrastructure.local.WeatherForecastEntity
import pe.edu.upc.viora.features.climate.infrastructure.remote.dto.DayForecastDto
import pe.edu.upc.viora.features.climate.infrastructure.remote.dto.WeatherForecastResponseDto
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

fun DayForecastDto.toEntity(plotId: String): WeatherForecastEntity = WeatherForecastEntity(
    plotId = plotId,
    forecastDate = forecastDate,
    maxTemperature = maxTemperature,
    minTemperature = minTemperature,
    precipitationProbability = precipitationProbability,
    windSpeedKmh = windSpeedKmh,
    isFrostRisk = isFrostRisk,
    syncedAt = syncedAt,
)

fun WeatherForecastEntity.toDomain(): DayForecast = DayForecast(
    date = LocalDate.parse(forecastDate),
    maxTempCelsius = maxTemperature,
    minTempCelsius = minTemperature,
    precipitationProbability = precipitationProbability,
    windSpeedKmh = windSpeedKmh,
    isFrostRisk = isFrostRisk,
    syncedAt = syncedAt.toInstantOrNull() ?: Instant.EPOCH,
)

fun List<WeatherForecastEntity>.toDomain(plotId: String, generatedAt: Instant? = null): WeatherForecast =
    WeatherForecast(
        plotId = PlotId(plotId),
        dailyForecasts = map { it.toDomain() }.sortedBy { it.date },
        generatedAt = generatedAt ?: firstOrNull()?.syncedAt.toInstantOrNull() ?: Instant.EPOCH,
    )

fun DayForecastDto.toDomain(): DayForecast = DayForecast(
    date = LocalDate.parse(forecastDate),
    maxTempCelsius = maxTemperature,
    minTempCelsius = minTemperature,
    precipitationProbability = precipitationProbability,
    windSpeedKmh = windSpeedKmh,
    isFrostRisk = isFrostRisk,
    syncedAt = syncedAt.toInstantOrNull() ?: Instant.EPOCH,
)

fun WeatherForecastResponseDto.toDomain(): WeatherForecast = WeatherForecast(
    plotId = PlotId(plotId),
    dailyForecasts = dailyForecasts.map { it.toDomain() }.sortedBy { it.date },
    generatedAt = generatedAt.toInstantOrNull() ?: Instant.EPOCH,
)

private fun String?.toInstantOrNull(): Instant? = this?.let { raw ->
    runCatching { Instant.parse(raw) }
        .recoverCatching { OffsetDateTime.parse(raw).toInstant() }
        .getOrNull()
}
