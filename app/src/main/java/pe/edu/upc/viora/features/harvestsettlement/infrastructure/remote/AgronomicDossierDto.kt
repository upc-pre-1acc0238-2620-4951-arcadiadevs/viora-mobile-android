package pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote

import kotlinx.serialization.Serializable

/** `POST/GET /plots/{plotId}/agronomic-dossiers`. Dates are ISO strings; every block is null when never recorded. */
@Serializable
data class AgronomicDossierDto(
    val id: String,
    val plotId: String,
    val holderName: String? = null,
    val campaignYear: Int,
    val plotName: String,
    val variety: String? = null,
    val areaHectares: Double? = null,
    val fruitLoad: FruitLoadDto? = null,
    val prescription: DossierPrescriptionDto? = null,
    val thinning: DossierThinningDto? = null,
    val harvest: DossierHarvestDto? = null,
    val climate: DossierClimateDto? = null,
    val history: DossierHistoryDto? = null,
    val verificationHash: String,
    val sealedAt: String,
    val pageCount: Int? = null,
    val sizeBytes: Long? = null,
    val fileName: String? = null,
)

@Serializable
data class FruitLoadDto(
    val preThinningFruitsPerShoot: Double? = null,
    val targetFruitsPerShoot: Double? = null,
    val overTargetPercentage: Double? = null,
)

@Serializable
data class DossierPrescriptionDto(
    val removalPercentage: Double? = null,
    val windowOpensOn: String? = null,
    val windowClosesOn: String? = null,
)

/** [compliance] is `EXECUTED_ON_TIME`, `EXECUTED_LATE` or `NOT_RECORDED`. */
@Serializable
data class DossierThinningDto(
    val actualRemovalPercentage: Double? = null,
    val executedOn: String? = null,
    val compliance: String? = null,
)

@Serializable
data class DossierHarvestDto(
    val totalKg: Double? = null,
    val receiptNumber: String? = null,
)

@Serializable
data class DossierClimateDto(
    val linkedNodes: Int? = null,
    val daysWithReadings: Int? = null,
)

@Serializable
data class DossierHistoryDto(
    val fromYear: Int? = null,
    val toYear: Int? = null,
)

@Serializable
data class SealDossierRequestDto(val campaignYear: Int)

/** `GET /agronomic-dossiers/verifications/{hash}`: 200 only when the code matches a stored dossier. */
@Serializable
data class DossierVerificationDto(
    val verificationHash: String,
    val plotName: String,
    val areaHectares: Double? = null,
    val holderName: String? = null,
    val sealedAt: String,
    val pageCount: Int? = null,
    val historyFromYear: Int? = null,
    val historyToYear: Int? = null,
)
