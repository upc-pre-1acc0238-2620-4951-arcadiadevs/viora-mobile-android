package pe.edu.upc.viora.features.telemetry.domain.entity

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SoilMoistureStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric

/**
 * The hourly readings of one plot inside a time window (US17), oldest first. Pure calculations
 * over the readings live here so the screens only format and draw.
 */
data class TelemetrySeries(
    val plotId: String,
    val readings: List<HourlyReading>,
) {
    val isEmpty: Boolean get() = readings.isEmpty()

    /** The points of [metric], skipping the hours the nodes did not report it. */
    fun points(metric: TelemetryMetric): List<MetricPoint> =
        readings.mapNotNull { reading -> reading.valueOf(metric)?.let { MetricPoint(reading.observedAt, it) } }

    /** The most recent measured value of [metric], with its timestamp (US17: "highlights the last value"). */
    fun latest(metric: TelemetryMetric): MetricPoint? = points(metric).lastOrNull()

    fun stats(metric: TelemetryMetric): MetricStats? {
        val points = points(metric)
        if (points.isEmpty()) return null
        return MetricStats(
            min = points.minBy { it.value },
            max = points.maxBy { it.value },
            average = points.sumOf { it.value } / points.size,
        )
    }

    /** Moments when the 30 cm probe jumped up: the producer irrigated. */
    fun irrigationEvents(): List<Instant> = SoilMoistureRules.irrigationEvents(points(TelemetryMetric.SOIL_MOISTURE))

    /** Status of the latest soil moisture, or null when the probe has not reported. */
    fun soilStatus(): SoilMoistureStatus? =
        latest(TelemetryMetric.SOIL_MOISTURE)?.let { SoilMoistureRules.statusOf(it.value) }

    /** When the soil is expected to reach the watch level at the current pace, or null if it is not drying. */
    fun projectedWatchLevelAt(): Instant? =
        SoilMoistureRules.projectWatchLevel(points(TelemetryMetric.SOIL_MOISTURE))

    /**
     * Average day and night temperature of each calendar day (US17, scenario 2). Daylight is
     * [DAYLIGHT_START_HOUR]..[DAYLIGHT_END_HOUR] in the plot's [zone]; the rest is night.
     */
    fun dailyThermalSummaries(zone: ZoneId): List<DailyThermalSummary> =
        points(TelemetryMetric.TEMPERATURE)
            .groupBy { it.observedAt.atZone(zone).toLocalDate() }
            .toSortedMap()
            .map { (date, points) ->
                val (day, night) = points.partition { isDaylight(it.observedAt, zone) }
                DailyThermalSummary(
                    date = date,
                    dayAverageCelsius = day.averageOrNull(),
                    nightAverageCelsius = night.averageOrNull(),
                )
            }

    /** Day, night and daily oscillation over the whole window. */
    fun thermalSummary(zone: ZoneId): ThermalSummary? {
        val (day, night) = points(TelemetryMetric.TEMPERATURE).partition { isDaylight(it.observedAt, zone) }
        val dayAverage = day.averageOrNull() ?: return null
        val nightAverage = night.averageOrNull() ?: return null
        return ThermalSummary(dayAverage, nightAverage)
    }

    private fun isDaylight(at: Instant, zone: ZoneId): Boolean =
        at.atZone(zone).hour in DAYLIGHT_START_HOUR until DAYLIGHT_END_HOUR

    private fun List<MetricPoint>.averageOrNull(): Double? =
        if (isEmpty()) null else sumOf { it.value } / size

    companion object {
        const val DAYLIGHT_START_HOUR = 6
        const val DAYLIGHT_END_HOUR = 18
    }
}

/** Average day and night temperature of one calendar day. */
data class DailyThermalSummary(
    val date: LocalDate,
    val dayAverageCelsius: Double?,
    val nightAverageCelsius: Double?,
)

/** Average day and night temperature of a window and the oscillation between them. */
data class ThermalSummary(
    val dayAverageCelsius: Double,
    val nightAverageCelsius: Double,
) {
    /** Daily thermal oscillation: the physiological stimulus of the crop. */
    val oscillationCelsius: Double get() = dayAverageCelsius - nightAverageCelsius
}
