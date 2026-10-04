package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

/** Top-level response for `GET /api/v1/agroclimatic-incidents` (`AgroclimaticIncidentsSummaryResource` of the backend). */
@Serializable
data class AgroclimaticIncidentsSummaryDto(
    val summary: SummaryCountsDto,
    val incidents: List<AgroclimaticIncidentItemDto> = emptyList(),
)
