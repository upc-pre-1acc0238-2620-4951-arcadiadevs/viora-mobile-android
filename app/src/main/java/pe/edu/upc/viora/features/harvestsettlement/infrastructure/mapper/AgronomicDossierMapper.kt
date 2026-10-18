package pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper

import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.json.Json
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.AgronomicDossier
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.CachedDossier
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierClimate
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierFruitLoad
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierHarvest
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierHistory
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierPrescription
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierThinning
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierVerification
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.AgronomicDossierEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.AgronomicDossierDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.DossierVerificationDto

private val dossierJson = Json { ignoreUnknownKeys = true }

fun AgronomicDossierDto.toDomain(): AgronomicDossier = AgronomicDossier(
    id = id,
    plotId = plotId,
    campaignYear = campaignYear,
    plotName = plotName,
    holderName = holderName?.takeIf { it.isNotBlank() },
    variety = variety,
    areaHectares = areaHectares,
    fruitLoad = fruitLoad?.let { DossierFruitLoad(it.preThinningFruitsPerShoot, it.overTargetPercentage) },
    prescription = prescription?.let {
        DossierPrescription(it.removalPercentage, it.windowOpensOn.toLocalDate(), it.windowClosesOn.toLocalDate())
    },
    thinning = thinning?.let {
        DossierThinning(
            actualRemovalPercentage = it.actualRemovalPercentage,
            executedOn = it.executedOn.toLocalDate(),
            recorded = it.compliance != NOT_RECORDED && it.executedOn != null,
        )
    },
    harvest = harvest?.let { DossierHarvest(it.totalKg, it.receiptNumber?.takeIf { number -> number.isNotBlank() }) },
    climate = climate?.let { DossierClimate(it.linkedNodes, it.daysWithReadings) },
    history = history?.let { DossierHistory(it.fromYear, it.toYear) },
    verificationHash = verificationHash.lowercase(),
    sealedAt = Instant.parse(sealedAt),
    pageCount = pageCount,
    sizeBytes = sizeBytes,
    fileName = fileName?.takeIf { it.isNotBlank() } ?: "Expediente_${plotName.filter { it.isLetterOrDigit() }}_$campaignYear.pdf",
)

fun AgronomicDossierDto.toEntity(pdfPath: String?, pdfSavedAtEpochMs: Long?): AgronomicDossierEntity = AgronomicDossierEntity(
    plotId = plotId,
    campaignYear = campaignYear,
    payloadJson = dossierJson.encodeToString(AgronomicDossierDto.serializer(), this),
    pdfPath = pdfPath,
    pdfSavedAtEpochMs = pdfSavedAtEpochMs,
)

fun AgronomicDossierEntity.toCached(): CachedDossier = CachedDossier(
    dossier = dossierJson.decodeFromString(AgronomicDossierDto.serializer(), payloadJson).toDomain(),
    pdfPath = pdfPath,
    pdfSavedAt = pdfSavedAtEpochMs?.let(Instant::ofEpochMilli),
)

fun DossierVerificationDto.toDomain(): DossierVerification = DossierVerification(
    verificationHash = verificationHash.lowercase(),
    plotName = plotName,
    areaHectares = areaHectares,
    holderName = holderName?.takeIf { it.isNotBlank() },
    sealedAt = Instant.parse(sealedAt),
    pageCount = pageCount,
    historyFromYear = historyFromYear,
    historyToYear = historyToYear,
)

private fun String?.toLocalDate(): LocalDate? = this?.let(LocalDate::parse)

private const val NOT_RECORDED = "NOT_RECORDED"
