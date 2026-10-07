package pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper

import java.time.LocalDate
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleEntity

fun DraftTreeSampleEntity.toDomain(): TreeSample = TreeSample(
    treeIdentifier = treeIdentifier,
    shootsCount = shootsCount,
    fruitSetCount = fruitSetCount,
    trunkCircumferenceCm = trunkCircumferenceCm,
    trunkDiameterMm = trunkDiameterMm,
    observedOn = runCatching { LocalDate.parse(observedOn) }.getOrDefault(LocalDate.now()),
    isSynced = isSynced,
)

fun TreeSample.toEntity(
    plotId: String,
    plotName: String,
    campaignYear: Int,
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
)
