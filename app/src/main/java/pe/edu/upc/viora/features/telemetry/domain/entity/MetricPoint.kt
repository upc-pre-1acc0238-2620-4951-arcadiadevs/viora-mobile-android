package pe.edu.upc.viora.features.telemetry.domain.entity

import java.time.Instant

/** A value of one metric at one moment. */
data class MetricPoint(val observedAt: Instant, val value: Double)

/** Minimum, maximum and average of a metric over a window. */
data class MetricStats(
    val min: MetricPoint,
    val max: MetricPoint,
    val average: Double,
)
