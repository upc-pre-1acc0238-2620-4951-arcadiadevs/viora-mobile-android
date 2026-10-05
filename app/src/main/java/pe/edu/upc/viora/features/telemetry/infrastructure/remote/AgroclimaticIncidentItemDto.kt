package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/** `AgroclimaticIncidentResource` list item representation of the backend (`/api/v1/agroclimatic-incidents`). */
@Serializable
data class AgroclimaticIncidentItemDto(
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
    val stressDurationMinutes: Long = 0,
    val snoozedUntil: String? = null,
)
