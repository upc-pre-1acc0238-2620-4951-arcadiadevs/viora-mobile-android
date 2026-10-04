package pe.edu.upc.viora.features.telemetry.domain.entity

data class WeeklyTrendPoint(
    val timestamp: String,
    val value: Double,
    val threshold: Double,
)
