package pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper

import java.time.Instant
import kotlinx.serialization.json.Json
import pe.edu.upc.viora.core.network.toLocalDateOrNull
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CachedPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CaliberProjection
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CurrentLoad
import pe.edu.upc.viora.features.croploadregulation.domain.entity.LoadBalance
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.YieldEstimate
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.CaliberProjectionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningProfileStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateBasis
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateStatus
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningPrescriptionEntity
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.CaliberProjectionDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.CurrentLoadDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ExecutionConfirmationResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.LoadBalanceDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.PrescriptionResponseDto
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.YieldEstimateDto

private val prescriptionJson = Json { ignoreUnknownKeys = true }

fun PrescriptionResponseDto.toDomain(): ThinningPrescription = ThinningPrescription(
    id = id,
    plotId = plotId,
    campaignYear = campaignYear,
    status = PrescriptionStatus.from(status),
    percentageToRemove = percentageToRemove,
    targetFruitsPerShoot = targetFruitsPerShoot,
    fullBloomOn = fullBloomOn.toLocalDateOrNull(),
    windowOpensOn = windowOpensOn.toLocalDateOrNull(),
    windowClosesOn = windowClosesOn.toLocalDateOrNull(),
    profileStatus = ThinningProfileStatus.from(profileStatus),
    blockers = blockers.mapNotNull(PrescriptionBlocker::from).toSet(),
    currentLoad = currentLoad?.toDomain(),
    sampledTrees = sampledTrees ?: 0,
    treesNeeded = treesNeeded ?: (ThinningPrescription.SAMPLE_SIZE - (sampledTrees ?: 0)).coerceAtLeast(0),
    lastSampledOn = lastSampledOn.toLocalDateOrNull(),
    windowOpensDaysAfterBloom = windowOpensDaysAfterBloom,
    windowClosesDaysAfterBloom = windowClosesDaysAfterBloom,
    yieldEstimate = yieldEstimate?.toDomain(),
)

/** A load the backend could not fully state is dropped: the screen shows no half-made bars. */
private fun CurrentLoadDto.toDomain(): CurrentLoad? {
    val current = fruitsPerShoot ?: return null
    val target = targetFruitsPerShoot ?: return null
    val state = LoadState.from(loadState) ?: return null
    return CurrentLoad(current, target, loadRatio ?: current / target, state)
}

private fun YieldEstimateDto.toDomain(): YieldEstimate = YieldEstimate(
    status = YieldEstimateStatus.from(status),
    sustainableKgPerHa = sustainableKgPerHa,
    estimatedKgPerHa = estimatedKgPerHa,
    basis = YieldEstimateBasis.from(basis),
    harvestCampaigns = harvestCampaigns ?: 0,
    requiredHarvestCampaigns = requiredHarvestCampaigns ?: 0,
)

/** [executionJson] is carried over from the cached row: downloading the plan again must not forget the thinning. */
fun PrescriptionResponseDto?.toEntity(plotId: String, campaignYear: Int, syncedAtEpochMs: Long, executionJson: String? = null) =
    ThinningPrescriptionEntity(
        plotId = plotId,
        campaignYear = campaignYear,
        payloadJson = this?.let { prescriptionJson.encodeToString(PrescriptionResponseDto.serializer(), it) },
        syncedAtEpochMs = syncedAtEpochMs,
        executionJson = executionJson,
    )

/** A cached payload that no longer parses is read as "never downloaded", so the screen asks the backend again. */
fun ThinningPrescriptionEntity.toCached(): CachedPrescription? {
    val prescription = payloadJson?.let { json ->
        runCatching { prescriptionJson.decodeFromString(PrescriptionResponseDto.serializer(), json) }
            .getOrNull()?.toDomain() ?: return null
    }
    return CachedPrescription(prescription, Instant.ofEpochMilli(syncedAtEpochMs))
}

fun ExecutionConfirmationResponseDto.toDomain(): ThinningExecution? {
    val executedOn = executedDate.toLocalDateOrNull() ?: return null
    return ThinningExecution(
        prescriptionId = prescriptionId,
        executedOn = executedOn,
        removalPercentage = actualRemovalPercentage,
        notes = notes?.takeIf { it.isNotBlank() },
        isOpportune = isOpportune,
        loadBalance = loadBalance?.toDomain(),
        caliberProjection = caliberProjection?.toDomain(),
        yieldEstimate = yieldEstimate?.toDomain(),
    )
}

/** Like the current load, a balance without its figures is dropped instead of half drawn. */
private fun LoadBalanceDto.toDomain(): LoadBalance? {
    val before = preThinningFruitsPerShoot ?: return null
    val residual = residualFruitsPerShoot ?: return null
    val target = targetFruitsPerShoot ?: return null
    val state = LoadState.from(loadState) ?: return null
    return LoadBalance(before, residual, target, loadRatio ?: residual / target, state)
}

private fun CaliberProjectionDto.toDomain(): CaliberProjection = CaliberProjection(
    status = CaliberProjectionStatus.from(status),
    mostLikelySizeGrade = mostLikelySizeGrade,
    // Fewer fruits per kg means bigger fruit: the low end of the interval is the big fruit.
    smallFruitSizeGrade = sizeGradeHigh,
    bigFruitSizeGrade = sizeGradeLow,
    confidenceLevel = confidenceLevel,
    calibrationObservations = calibrationObservations ?: 0,
    requiredObservations = requiredObservations ?: 0,
    calibratedLoadMin = calibratedLoadMin,
    calibratedLoadMax = calibratedLoadMax,
)

fun ExecutionConfirmationResponseDto.toJson(): String =
    prescriptionJson.encodeToString(ExecutionConfirmationResponseDto.serializer(), this)

/** A row whose plan no longer parses is shown as never sampled until the next download replaces it. */
fun ThinningPrescriptionEntity.toCampaignPlan(): CampaignPlan =
    CampaignPlan(plotId = plotId, prescription = toCached()?.prescription, execution = toExecution())

/** A cached thinning that no longer parses is read as missing, so P63 asks the backend again. */
fun ThinningPrescriptionEntity.toExecution(): ThinningExecution? = executionJson?.let { json ->
    runCatching { prescriptionJson.decodeFromString(ExecutionConfirmationResponseDto.serializer(), json) }.getOrNull()?.toDomain()
}
