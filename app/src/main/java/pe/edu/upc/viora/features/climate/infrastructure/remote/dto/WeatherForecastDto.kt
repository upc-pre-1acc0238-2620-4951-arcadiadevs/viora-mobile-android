package pe.edu.upc.viora.features.climate.infrastructure.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherForecastResponseDto(
    @SerialName("plotId") val plotId: String,
    @SerialName("dailyForecasts") val dailyForecasts: List<DayForecastDto>,
    @SerialName("generatedAt") val generatedAt: String,
)

@Serializable
data class DayForecastDto(
    @SerialName("forecastDate") val forecastDate: String,
    @SerialName("maxTemperature") val maxTemperature: Double,
    @SerialName("minTemperature") val minTemperature: Double,
    @SerialName("precipitationProbability") val precipitationProbability: Double,
    @SerialName("windSpeedKmh") val windSpeedKmh: Double,
    @SerialName("isFrostRisk") val isFrostRisk: Boolean,
    @SerialName("syncedAt") val syncedAt: String,
)
