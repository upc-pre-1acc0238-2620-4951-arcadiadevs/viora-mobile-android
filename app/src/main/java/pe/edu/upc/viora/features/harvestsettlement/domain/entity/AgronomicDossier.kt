package pe.edu.upc.viora.features.harvestsettlement.domain.entity

import java.time.Instant
import java.time.LocalDate

/**
 * The agronomic dossier the producer sealed for a settled campaign (US30, P76): the values its PDF states and the
 * SHA-256 that proves the PDF was not changed. Every value is null when the platform never had it ("Sin registro").
 */
data class AgronomicDossier(
    val id: String,
    val plotId: String,
    val campaignYear: Int,
    val plotName: String,
    val holderName: String?,
    val variety: String?,
    val areaHectares: Double?,
    val fruitLoad: DossierFruitLoad?,
    val prescription: DossierPrescription?,
    val thinning: DossierThinning?,
    val harvest: DossierHarvest?,
    val climate: DossierClimate?,
    val history: DossierHistory?,
    val verificationHash: String,
    val sealedAt: Instant,
    val pageCount: Int?,
    val sizeBytes: Long?,
    val fileName: String,
)

/** Crop load before thinning, in fruits per shoot, and how far above the target it was. */
data class DossierFruitLoad(val preThinningFruitsPerShoot: Double?, val overTargetPercentage: Double?)

/** The share the plan told to remove and its window. */
data class DossierPrescription(val removalPercentage: Double?, val windowOpensOn: LocalDate?, val windowClosesOn: LocalDate?)

/** The thinning recorded in the field; [recorded] is false when the producer never recorded one. */
data class DossierThinning(val actualRemovalPercentage: Double?, val executedOn: LocalDate?, val recorded: Boolean)

/** The settled harvest and its mill receipt. */
data class DossierHarvest(val totalKg: Double?, val receiptNumber: String?)

/** Climate nodes linked to the plot and the days with readings behind the campaign. */
data class DossierClimate(val linkedNodes: Int?, val daysWithReadings: Int?)

/** The harvested campaigns the dossier includes. */
data class DossierHistory(val fromYear: Int?, val toYear: Int?)

/**
 * A dossier as the phone keeps it: the data, and the PDF once it was downloaded ([pdfPath], saved at [pdfSavedAt]),
 * so the last version can still be opened without signal.
 */
data class CachedDossier(
    val dossier: AgronomicDossier,
    val pdfPath: String?,
    val pdfSavedAt: Instant?,
)

/** Proof that a SHA-256 matches a dossier stored by Viora (US30 scenario 2, P78). */
data class DossierVerification(
    val verificationHash: String,
    val plotName: String,
    val areaHectares: Double?,
    val holderName: String?,
    val sealedAt: Instant,
    val pageCount: Int?,
    val historyFromYear: Int?,
    val historyToYear: Int?,
)
