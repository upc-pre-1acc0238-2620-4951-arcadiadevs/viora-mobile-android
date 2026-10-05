package pe.edu.upc.viora.features.telemetry.domain.entity

/**
 * A single daily data point in the 7-day microclimatic progression curve.
 * [value] is plotted against [threshold] to highlight the anomalous threshold breach.
 */
data class WeeklyTrendPoint(
    val timestamp: String,
    val value: Double,
    val threshold: Double,
)
