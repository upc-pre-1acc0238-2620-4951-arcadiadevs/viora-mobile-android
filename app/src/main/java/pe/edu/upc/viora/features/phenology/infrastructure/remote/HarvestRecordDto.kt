package pe.edu.upc.viora.features.phenology.infrastructure.remote

import kotlinx.serialization.Serializable

/** DTO for `/api/v1/plots/{plotId}/harvest-records`. */
@Serializable
data class HarvestRecordDto(
    val id: String,
    val plotId: String,
    val campaignYear: Int,
    val totalYieldKg: Double,
    val greenKg: Double? = null,
    val blackKg: Double? = null,
    val bearingClassification: String? = null,
    val calculatedBbi: Double? = null,
    val recordedAt: String? = null,
)
