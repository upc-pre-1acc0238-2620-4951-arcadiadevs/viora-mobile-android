package pe.edu.upc.viora.features.telemetry.presentation.state

import java.time.LocalDate
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SkyCondition

/** What the Home climate cards show; a null part draws its card without numbers ("—"). */
data class HomeClimateUiState(
    val weather: HomeWeather? = null,
    val soil: HomeSoil? = null,
)

/**
 * The "Clima · ahora" card. [isLiveReading] is false when there is no recent sensor reading and
 * [temperatureCelsius] is today's forecast high, so the card says "Hoy" instead of "Ahora".
 */
data class HomeWeather(
    val temperatureCelsius: Double,
    val isLiveReading: Boolean,
    val sky: SkyCondition?,
    val maxCelsius: Double?,
    val minCelsius: Double?,
    val hotDay: HomeHotDay?,
)

/** The hottest coming day of the week when it is warm or worse ("Jue 34°"). */
data class HomeHotDay(val date: LocalDate, val maxCelsius: Double, val isExtreme: Boolean)

/** The "Suelo · humedad" card: the latest reading of the 30 cm probe (always drawn in green, as in Figma). */
data class HomeSoil(val percent: Double)
