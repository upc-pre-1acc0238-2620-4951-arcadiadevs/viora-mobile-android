package pe.edu.upc.viora.features.climate.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity

/**
 * Cached copy of a daily weather forecast in the Room database.
 * Uses a composite primary key of (plot_id, forecast_date).
 */
@Entity(
    tableName = "weather_forecasts",
    primaryKeys = ["plot_id", "forecast_date"],
)
data class WeatherForecastEntity(
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "forecast_date")
    val forecastDate: String,
    @ColumnInfo(name = "max_temperature")
    val maxTemperature: Double,
    @ColumnInfo(name = "min_temperature")
    val minTemperature: Double,
    @ColumnInfo(name = "precipitation_probability")
    val precipitationProbability: Double,
    @ColumnInfo(name = "wind_speed_kmh")
    val windSpeedKmh: Double,
    @ColumnInfo(name = "is_frost_risk")
    val isFrostRisk: Boolean,
    @ColumnInfo(name = "synced_at")
    val syncedAt: String,
)
