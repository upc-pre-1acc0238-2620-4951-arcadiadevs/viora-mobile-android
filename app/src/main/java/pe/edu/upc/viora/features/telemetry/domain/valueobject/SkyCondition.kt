package pe.edu.upc.viora.features.telemetry.domain.valueobject

/**
 * Sky of a forecast day. The API does not send a weather code, so it is derived from the
 * probability of rain (see `ForecastDay.sky`).
 */
enum class SkyCondition {
    SUNNY,
    PARTLY_CLOUDY,
    RAINY,
}
