package pe.edu.upc.viora.features.telemetry.domain.entity

import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

data class IncidentDetail(
    val id: String,
    val plotId: String,
    val plotName: String,
    val plotVariety: String,
    val type: IncidentType,
    val severity: IncidentSeverity,
    val status: IncidentStatus,
    val headlineKey: String,
    val metricName: String,
    val currentValue: Double,
    val thresholdValue: Double,
    val unit: String,
    val triggeredAt: String,
    val dateFormatted: String,
    val timeWindow: String,
    val stressDurationMinutes: Long,
    val snoozedUntil: String? = null,
    val mitigationSteps: List<MitigationStep> = emptyList(),
    val weeklyTrend: List<WeeklyTrendPoint> = emptyList(),
)
