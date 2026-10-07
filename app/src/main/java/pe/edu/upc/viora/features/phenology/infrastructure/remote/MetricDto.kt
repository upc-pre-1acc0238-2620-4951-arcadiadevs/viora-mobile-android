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
    val portionsAccumulated: Double? = null,
    val thresholdPortions: Double? = null,
    val daysAbove24Celsius: Int? = null,
    val daysAbove24C: Int? = null,
    val seasonState: String? = null,
    val projectedCompletionDate: String? = null,
    val previousWinterCompletionDate: String? = null,
    val ensoRisk: String? = null,
    val curvePoints: List<ChillCurvePointDto>? = null,
)

@Serializable
data class ChillCurvePointDto(
    val date: String,
    val accumulatedThisYear: Double,
    val accumulatedPreviousYear: Double? = null,
)
