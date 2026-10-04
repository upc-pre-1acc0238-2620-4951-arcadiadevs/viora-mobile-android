package pe.edu.upc.viora.features.telemetry.domain.entity

data class AlertsSummary(
    val activeCount: Long = 0,
    val criticalCount: Long = 0,
    val warningCount: Long = 0,
    val normalizedCount: Long = 0,
)
