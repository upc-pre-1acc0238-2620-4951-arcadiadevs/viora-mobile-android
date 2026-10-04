package pe.edu.upc.viora.features.telemetry.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** Cached copy of a virtual sensor node in the Room database. */
@Entity(tableName = "sensor_nodes")
data class SensorNodeEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    val name: String,
    val type: String,
    @ColumnInfo(name = "depth_cm")
    val depthCm: Int?,
    val status: String,
    @ColumnInfo(name = "last_reading_at")
    val lastReadingAt: String?,
    @ColumnInfo(name = "last_temperature_celsius")
    val lastTemperatureCelsius: Double?,
    @ColumnInfo(name = "last_humidity_percent")
    val lastHumidityPercent: Double?,
)
