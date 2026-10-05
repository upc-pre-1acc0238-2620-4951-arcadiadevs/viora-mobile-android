package pe.edu.upc.viora.features.phenology.infrastructure.remote

import kotlinx.serialization.Serializable

/** DTO for `/api/v1/plots/{plotId}/metrics`. Unknown `details` keys are ignored. */
@Serializable
data class MetricDto(
    val metricName: String,
    val value: Double? = null,
    val qualitativeCategory: String? = null,
    val details: MetricDetailsDto? = null,
    val evaluatedAt: String? = null,
)

@Serializable
data class MetricDetailsDto(
    val evaluatedYearsCount: Int? = null,
    val sampleSufficiency: String? = null,
)
