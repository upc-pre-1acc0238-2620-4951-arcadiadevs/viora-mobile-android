package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Envelope for `GET /api/v1/thinning-events`.
 */
@Serializable
data class ThinningEventsResponseDto(
    val events: List<ThinningEventItemDto> = emptyList(),
)
