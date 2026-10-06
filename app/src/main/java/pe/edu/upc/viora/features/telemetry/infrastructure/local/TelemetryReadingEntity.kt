package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Cached hourly reading of a plot. The id is `plotId|observedAtEpochMs`, so a re-download overwrites it. */
@Entity(tableName = "telemetry_readings")
data class TelemetryReadingEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "observed_at_epoch_ms")
    val observedAtEpochMs: Long,
    @ColumnInfo(name = "air_temperature_celsius")
    val airTemperatureCelsius: Double?,
    @ColumnInfo(name = "relative_humidity_percent")
    val relativeHumidityPercent: Double?,
    @ColumnInfo(name = "soil_moisture_30cm_percent")
    val soilMoisture30cmPercent: Double?,
    @ColumnInfo(name = "soil_moisture_60cm_percent")
    val soilMoisture60cmPercent: Double?,
)
