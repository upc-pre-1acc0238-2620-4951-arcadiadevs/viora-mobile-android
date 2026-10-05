package pe.edu.upc.viora.features.phenology.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class RectifyHarvestYieldRequestDto(
    val totalYieldKg: Double,
    val greenKg: Double? = null,
    val blackKg: Double? = null,
    val notes: String? = null,
)
