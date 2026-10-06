package pe.edu.upc.viora.features.climate.domain.entity

import java.time.Instant
import java.time.LocalDate

/**
 * Daily weather forecast for an agricultural plot (Open-Meteo via Viora backend).
 *
 * @property date Calendar date of the forecast.
 * @property maxTempCelsius Maximum forecasted temperature in degrees Celsius.
 * @property minTempCelsius Minimum forecasted temperature in degrees Celsius.
 * @property precipitationProbability Probability of precipitation in percentage (0 to 100).
 * @property windSpeedKmh Forecasted maximum wind speed in kilometers per hour.
 * @property isFrostRisk True when thermal conditions threaten frost damage to olive flowers or vegetative shoots.
 * @property syncedAt Timestamp when the backend retrieved and processed this forecast point.
 */
data class DayForecast(
    val date: LocalDate,
    val maxTempCelsius: Double,
    val minTempCelsius: Double,
    val precipitationProbability: Double,
    val windSpeedKmh: Double,
    val isFrostRisk: Boolean,
    val syncedAt: Instant,
)
