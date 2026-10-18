package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/** `GET /plots/{plotId}/thinning-prescriptions` and `PUT …/full-bloom`. Dates are ISO strings. */
@Serializable
data class PrescriptionResponseDto(
    val id: String,
    val plotId: String,
    val campaignYear: Int,
    val targetFruitsPerShoot: Double? = null,
    val percentageToRemove: Double? = null,
    val status: String,
    val fullBloomOn: String? = null,
    val windowOpensOn: String? = null,
    val windowClosesOn: String? = null,
    val profileStatus: String? = null,
    val blockers: List<String> = emptyList(),
    val currentLoad: CurrentLoadDto? = null,
    val sampledTrees: Int? = null,
    val treesNeeded: Int? = null,
    val lastSampledOn: String? = null,
    val windowOpensDaysAfterBloom: Int? = null,
    val windowClosesDaysAfterBloom: Int? = null,
    val yieldEstimate: YieldEstimateDto? = null,
)

@Serializable
data class CurrentLoadDto(
    val fruitsPerShoot: Double? = null,
    val targetFruitsPerShoot: Double? = null,
    val loadRatio: Double? = null,
    val loadState: String? = null,
)

@Serializable
data class YieldEstimateDto(
    val status: String? = null,
    val sustainableKgPerHa: Double? = null,
    val estimatedKgPerHa: Double? = null,
    val basis: String? = null,
    val harvestCampaigns: Int? = null,
    val requiredHarvestCampaigns: Int? = null,
)

/** Body of `PUT /plots/{plotId}/thinning-prescriptions/full-bloom`. */
@Serializable
data class RecordFullBloomRequestDto(
    val campaignYear: Int,
    val observedOn: String,
)
