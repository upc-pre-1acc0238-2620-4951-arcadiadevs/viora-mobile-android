package pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper

import java.time.Clock
import java.time.LocalDate
import pe.edu.upc.viora.features.croploadregulation.domain.entity.DraftSample
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleEntity

/** [clock] dates a draft whose stored day cannot be read. */
fun DraftTreeSampleEntity.toDomain(clock: Clock): TreeSample = TreeSample(
    treeIdentifier = treeIdentifier,
    shootsCount = shootsCount,
    fruitSetCount = fruitSetCount,
    trunkCircumferenceCm = trunkCircumferenceCm,
    trunkDiameterMm = trunkDiameterMm,
    observedOn = runCatching { LocalDate.parse(observedOn) }.getOrElse { LocalDate.now(clock) },
    isSynced = isSynced,
)

fun TreeSample.toEntity(
    plotId: String,
    plotName: String,
    campaignYear: Int,
    createdAt: Long,
    batchId: String? = null,
): DraftTreeSampleEntity = DraftTreeSampleEntity(
    id = "$plotId-$treeIdentifier",
    plotId = plotId,
    plotName = plotName,
    campaignYear = campaignYear,
    treeIdentifier = treeIdentifier,
    shootsCount = shootsCount,
    fruitSetCount = fruitSetCount,
    trunkCircumferenceCm = trunkCircumferenceCm,
    trunkDiameterMm = trunkDiameterMm,
    observedOn = observedOn.toString(),
    isSynced = isSynced,
    createdAt = createdAt,
    batchId = batchId,
)

fun DraftTreeSampleEntity.toDraft(clock: Clock): DraftSample = DraftSample(
    id = id,
    plotId = plotId,
    plotName = plotName,
    campaignYear = campaignYear,
    sample = toDomain(clock),
    batchId = batchId,
)
