package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class SummaryCountsDto(
    val activeCount: Long = 0,
    val criticalCount: Long = 0,
    val warningCount: Long = 0,
    val normalizedCount: Long = 0,
)
