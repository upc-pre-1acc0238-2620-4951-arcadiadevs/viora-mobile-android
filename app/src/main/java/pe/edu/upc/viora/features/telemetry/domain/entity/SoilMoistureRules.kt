package pe.edu.upc.viora.features.telemetry.domain.entity

import java.time.Duration
import java.time.Instant
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SoilMoistureStatus

/**
 * Business rules about the volumetric soil moisture of the root zone, in percent.
 * Pure Kotlin: the thresholds come from the report (US18: recharge point below 18 %).
 */
object SoilMoistureRules {

    /** Below this the plot is under hydric stress and needs an urgent irrigation. */
    const val RECHARGE_POINT_PERCENT = 18.0

    /** Margin above the recharge point where the irrigation should already be planned. */
    const val WATCH_MARGIN_PERCENT = 4.0

    /** Level at which the producer is advised to have irrigated (recharge point + margin). */
    const val WATCH_LEVEL_PERCENT = RECHARGE_POINT_PERCENT + WATCH_MARGIN_PERCENT

    /** A rise of at least this many points between two close readings is an irrigation. */
    const val IRRIGATION_JUMP_PERCENT = 4.0

    private val MAX_IRRIGATION_GAP: Duration = Duration.ofHours(3)
    private const val MIN_POINTS_FOR_PROJECTION = 6
    private const val MIN_DRYING_PERCENT_PER_HOUR = 0.01
    private val MAX_PROJECTION: Duration = Duration.ofDays(7)

    fun statusOf(percent: Double): SoilMoistureStatus = when {
        percent < RECHARGE_POINT_PERCENT -> SoilMoistureStatus.STRESS
        percent <= WATCH_LEVEL_PERCENT -> SoilMoistureStatus.WATCH
        else -> SoilMoistureStatus.IN_RANGE
    }

    /** The moments (the later reading of each pair) where the moisture rose by an irrigation. */
    fun irrigationEvents(points: List<MetricPoint>): List<Instant> =
        points.zipWithNext().mapNotNull { (before, after) ->
            val rose = after.value - before.value >= IRRIGATION_JUMP_PERCENT
            val close = Duration.between(before.observedAt, after.observedAt) <= MAX_IRRIGATION_GAP
            after.observedAt.takeIf { rose && close }
        }

    /**
     * When the soil would reach [WATCH_LEVEL_PERCENT] if it keeps drying at the pace it has had
     * since the last irrigation (least squares line). Null when there are too few readings, the
     * soil is not drying, it is already at or below the level, or the date is over a week away.
     */
    fun projectWatchLevel(points: List<MetricPoint>): Instant? {
        val last = points.lastOrNull() ?: return null
        if (last.value <= WATCH_LEVEL_PERCENT) return null

        val lastIrrigation = irrigationEvents(points).lastOrNull()
        val sinceIrrigation = if (lastIrrigation == null) points else points.filter { it.observedAt >= lastIrrigation }
        if (sinceIrrigation.size < MIN_POINTS_FOR_PROJECTION) return null

        val origin = sinceIrrigation.first().observedAt
        val xs = sinceIrrigation.map { Duration.between(origin, it.observedAt).toMinutes() / MINUTES_PER_HOUR }
        val ys = sinceIrrigation.map { it.value }
        val meanX = xs.average()
        val meanY = ys.average()
        val variance = xs.sumOf { (it - meanX) * (it - meanX) }
        if (variance == 0.0) return null
        val slopePerHour = xs.indices.sumOf { (xs[it] - meanX) * (ys[it] - meanY) } / variance
        if (slopePerHour > -MIN_DRYING_PERCENT_PER_HOUR) return null

        val hoursLeft = (last.value - WATCH_LEVEL_PERCENT) / -slopePerHour
        val eta = Duration.ofMinutes((hoursLeft * MINUTES_PER_HOUR).toLong())
        return if (eta > MAX_PROJECTION) null else last.observedAt.plus(eta)
    }

    private const val MINUTES_PER_HOUR = 60.0
}
