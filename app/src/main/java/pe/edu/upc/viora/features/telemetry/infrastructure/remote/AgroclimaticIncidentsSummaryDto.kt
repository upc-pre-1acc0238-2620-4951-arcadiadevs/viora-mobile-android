package pe.edu.upc.viora.features.telemetry.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class AgroclimaticIncidentsSummaryDto(
    val summary: SummaryCountsDto,
    val incidents: List<AgroclimaticIncidentItemDto> = emptyList(),
)
