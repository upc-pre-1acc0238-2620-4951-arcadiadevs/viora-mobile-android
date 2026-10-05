package pe.edu.upc.viora.features.telemetry.domain.entity

/**
 * Quantitative counter summary for alerts across all plots.
 * Powers the notification bell badge, Home alerts widget and filter capsules.
 */
data class AlertsSummary(
    val activeCount: Long = 0,
    val criticalCount: Long = 0,
    val warningCount: Long = 0,
    val normalizedCount: Long = 0,
)
