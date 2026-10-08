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

/**
 * Details of a metric. BBI uses the first two fields; the others belong to `EREZ_CHILLING_PORTIONS`
 * (see `MetricResource` in viora-platform).
 */
@Serializable
data class MetricDetailsDto(
    val evaluatedYearsCount: Int? = null,
    val sampleSufficiency: String? = null,
    val thresholdTarget: Double? = null,
    val seasonYear: Int? = null,
    val evaluatedThrough: String? = null,
    val completionDate: String? = null,
    val seasonState: String? = null,
    val daysAbove24Celsius: Int? = null,
    val currentWarmStreakDays: Int? = null,
    val longestWarmStreakDays: Int? = null,
    val thermalAnomaly: String? = null,
    val projectionStatus: String? = null,
    val projectedCompletionDate: String? = null,
    val previousSeason: PreviousChillSeasonDto? = null,
    val dailyCurve: List<ChillCurvePointDto>? = null,
)

@Serializable
data class PreviousChillSeasonDto(
    val seasonYear: Int,
    val accumulatedPortions: Double,
    val completionDate: String? = null,
)

@Serializable
data class ChillCurvePointDto(
    val date: String,
    val portions: Double? = null,
    val previousSeasonPortions: Double? = null,
)
