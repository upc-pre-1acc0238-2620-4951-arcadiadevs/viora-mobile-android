package pe.edu.upc.viora.features.telemetry.domain.entity

import java.time.LocalDate
import pe.edu.upc.viora.features.telemetry.domain.valueobject.HeatLevel
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SkyCondition

/** The forecast of one calendar day for the plot's coordinates (`WeatherForecastDay`, report table 73). */
data class ForecastDay(
    val date: LocalDate,
    val maxTemperatureCelsius: Double,
    val minTemperatureCelsius: Double,
    val precipitationProbabilityPercent: Int,
    val windSpeedKmh: Double,
) {
    /** Sky derived from the chance of rain: the API does not send a weather code. */
    val sky: SkyCondition
        get() = when {
            precipitationProbabilityPercent >= RAINY_FROM_PERCENT -> SkyCondition.RAINY
            precipitationProbabilityPercent >= CLOUDY_FROM_PERCENT -> SkyCondition.PARTLY_CLOUDY
            else -> SkyCondition.SUNNY
        }

    val heat: HeatLevel
        get() = when {
            maxTemperatureCelsius >= EXTREME_HEAT_CELSIUS -> HeatLevel.EXTREME
            maxTemperatureCelsius >= WARM_CELSIUS -> HeatLevel.WARM
            else -> HeatLevel.NORMAL
        }

    companion object {
        const val CLOUDY_FROM_PERCENT = 10
        const val RAINY_FROM_PERCENT = 50
        const val WARM_CELSIUS = 30.0

        /** Above this during flowering the stigmas dry out (US18, scenario 2). */
        const val EXTREME_HEAT_CELSIUS = 32.0
    }
}
