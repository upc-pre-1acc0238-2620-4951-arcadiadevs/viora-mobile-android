package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Cached forecast day of a plot. The id is `plotId|date`. All the days of a download share [syncedAtEpochMs]. */
@Entity(tableName = "forecast_days")
data class ForecastDayEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "forecast_date")
    val forecastDate: String,
    @ColumnInfo(name = "max_temperature_celsius")
    val maxTemperatureCelsius: Double,
    @ColumnInfo(name = "min_temperature_celsius")
    val minTemperatureCelsius: Double,
    @ColumnInfo(name = "precipitation_probability_percent")
    val precipitationProbabilityPercent: Double,
    @ColumnInfo(name = "wind_speed_kmh")
    val windSpeedKmh: Double,
    @ColumnInfo(name = "synced_at_epoch_ms")
    val syncedAtEpochMs: Long,
)
