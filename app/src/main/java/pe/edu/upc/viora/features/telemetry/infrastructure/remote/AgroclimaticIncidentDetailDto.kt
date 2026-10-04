package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/** Detailed payload of `GET /api/v1/agroclimatic-incidents/{incidentId}` (`AgroclimaticIncidentDetailResource` of the backend). */
@Serializable
data class AgroclimaticIncidentDetailDto(
    val id: String,
    val plotId: String,
    val plotName: String = "",
    val plotVariety: String = "",
    val type: String,
    val severity: String,
    val status: String,
    val headlineKey: String = "",
    val metricName: String = "",
    val currentValue: Double = 0.0,
    val thresholdValue: Double = 0.0,
    val unit: String = "",
    val triggeredAt: String = "",
    val dateFormatted: String = "",
    val timeWindow: String = "",
    val stressDurationMinutes: Long = 0,
    val snoozedUntil: String? = null,
    val mitigationSteps: List<MitigationStepDto> = emptyList(),
    val weeklyTrend: List<WeeklyTrendPointDto> = emptyList(),
)
