package pe.edu.upc.viora.features.phenology.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class RecordHarvestYieldRequestDto(
    val campaignYear: Int,
    val totalYieldKg: Double,
    val greenKg: Double? = null,
    val blackKg: Double? = null,
    val notes: String? = null,
)
