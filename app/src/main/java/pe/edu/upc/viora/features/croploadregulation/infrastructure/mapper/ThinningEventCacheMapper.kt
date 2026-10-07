package pe.edu.upc.viora.features.croploadregulation.infrastructure.mapper

import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningEventEntity
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ThinningEventItemDto

/**
 * The cache keeps the server's own values (text dates, raw enum names), so a cached event is read
 * back through the same [ThinningEventItemDto.toDomain] as a fresh one.
 */
fun ThinningEventItemDto.toEntity(): ThinningEventEntity = ThinningEventEntity(
    id = id,
    eventType = eventType,
    prescriptionId = prescriptionId,
    confirmationId = confirmationId,
    plotId = plotId,
    plotName = plotName,
    campaignYear = campaignYear,
    occurredAt = occurredAt,
    evaluatedTreesCount = evaluatedTreesCount,
    totalShootsCount = totalShootsCount,
    totalFruitsCount = totalFruitsCount,
    meanFruitsPerShoot = meanFruitsPerShoot,
    isRepresentative = isRepresentative,
    removalPercentage = removalPercentage,
    removedKg = removedKg,
    executedDate = executedDate,
    laborCrewSize = laborCrewSize,
    timeliness = timeliness,
)

fun ThinningEventEntity.toDomain(): ThinningEvent = ThinningEventItemDto(
    id = id,
    eventType = eventType,
    prescriptionId = prescriptionId,
    confirmationId = confirmationId,
    plotId = plotId,
    plotName = plotName,
    campaignYear = campaignYear,
    occurredAt = occurredAt,
    evaluatedTreesCount = evaluatedTreesCount,
    totalShootsCount = totalShootsCount,
    totalFruitsCount = totalFruitsCount,
    meanFruitsPerShoot = meanFruitsPerShoot,
    isRepresentative = isRepresentative,
    removalPercentage = removalPercentage,
    removedKg = removedKg,
    executedDate = executedDate,
    laborCrewSize = laborCrewSize,
    timeliness = timeliness,
).toDomain()
