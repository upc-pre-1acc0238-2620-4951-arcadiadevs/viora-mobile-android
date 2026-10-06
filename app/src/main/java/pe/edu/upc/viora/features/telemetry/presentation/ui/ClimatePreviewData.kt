package pe.edu.upc.viora.features.telemetry.presentation.ui

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.sin
import pe.edu.upc.viora.features.telemetry.domain.entity.ForecastDay
import pe.edu.upc.viora.features.telemetry.domain.entity.HourlyReading
import pe.edu.upc.viora.features.telemetry.domain.entity.TelemetrySeries
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast

// Sample data for the @Preview functions of the climate screens. Deterministic on purpose.

internal val PreviewZone: ZoneId = ZoneId.of("America/Lima")
internal val PreviewNow: Instant = Instant.parse("2026-10-06T15:30:00Z") // Tuesday 10:30 in Lima
internal val PreviewToday: LocalDate = PreviewNow.atZone(PreviewZone).toLocalDate()

/** [hours] of hourly readings ending at [PreviewNow]: a daily temperature cycle and a soil that dries and is irrigated. */
internal fun previewSeries(hours: Int = 24 * 7): TelemetrySeries {
    val readings = (hours downTo 0).map { back ->
        val at = PreviewNow.minus(Duration.ofHours(back.toLong()))
        val hourOfDay = at.atZone(PreviewZone).hour
        val temperature = 20.0 + 7.0 * sin((hourOfDay - 9) / 24.0 * 2 * PI)
        val hoursSinceStart = hours - back
        // Dries about 0.18 points per hour and is irrigated once, 60 hours into the window.
        val irrigated = hoursSinceStart >= IRRIGATION_AT_HOUR
        val soil = if (irrigated) 38.0 - (hoursSinceStart - IRRIGATION_AT_HOUR) * 0.07 else 30.0 - hoursSinceStart * 0.1
        HourlyReading(
            observedAt = at,
            airTemperatureCelsius = temperature,
            relativeHumidityPercent = 45.0 - 10.0 * sin((hourOfDay - 9) / 24.0 * 2 * PI),
            soilMoisture30cmPercent = soil.coerceAtLeast(10.0),
            soilMoisture60cmPercent = soil.coerceAtLeast(10.0) + 4,
        )
    }
    return TelemetrySeries(plotId = "plot-1", readings = readings)
}

internal fun previewForecast(): WeatherForecast {
    val maxima = listOf(29.0, 31.0, 34.0, 31.0, 28.0, 27.0, 28.0)
    val rain = listOf(0, 0, 0, 0, 10, 20, 0)
    return WeatherForecast(
        plotId = "plot-1",
        days = maxima.indices.map { index ->
            ForecastDay(
                date = PreviewToday.plusDays(index.toLong()),
                maxTemperatureCelsius = maxima[index],
                minTemperatureCelsius = 14.0 + index % 3,
                precipitationProbabilityPercent = rain[index],
                windSpeedKmh = 10.0 + index * 2,
            )
        },
        syncedAt = PreviewNow.minus(Duration.ofHours(4)),
    )
}

private const val IRRIGATION_AT_HOUR = 60
