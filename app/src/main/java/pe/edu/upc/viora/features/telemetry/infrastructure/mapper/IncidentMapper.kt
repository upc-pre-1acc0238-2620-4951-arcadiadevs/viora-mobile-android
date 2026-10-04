package pe.edu.upc.viora.features.telemetry.infrastructure.mapper

import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail
import pe.edu.upc.viora.features.telemetry.domain.entity.MitigationStep
import pe.edu.upc.viora.features.telemetry.domain.entity.WeeklyTrendPoint
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType
import pe.edu.upc.viora.features.telemetry.infrastructure.local.IncidentEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.AgroclimaticIncidentDetailDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.AgroclimaticIncidentItemDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.MitigationStepDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.SummaryCountsDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.WeeklyTrendPointDto

fun AgroclimaticIncidentItemDto.toEntity(): IncidentEntity = IncidentEntity(
    id = id,
    plotId = plotId,
    plotName = plotName,
    plotVariety = plotVariety,
    type = type,
    severity = severity,
    status = status,
    headlineKey = headlineKey,
    metricName = metricName,
    currentValue = currentValue,
    thresholdValue = thresholdValue,
    unit = unit,
    triggeredAt = triggeredAt,
    stressDurationMinutes = stressDurationMinutes,
    snoozedUntil = snoozedUntil,
)

fun IncidentEntity.toDomain(): AgroclimaticIncident = AgroclimaticIncident(
    id = id,
    plotId = plotId,
    plotName = plotName,
    plotVariety = plotVariety,
    type = IncidentType.fromString(type),
    severity = IncidentSeverity.fromString(severity),
    status = IncidentStatus.fromString(status),
    headlineKey = headlineKey,
    metricName = metricName,
    currentValue = currentValue,
    thresholdValue = thresholdValue,
    unit = unit,
    triggeredAt = triggeredAt,
    stressDurationMinutes = stressDurationMinutes,
    snoozedUntil = snoozedUntil,
)

fun AgroclimaticIncidentItemDto.toDomain(): AgroclimaticIncident = AgroclimaticIncident(
    id = id,
    plotId = plotId,
    plotName = plotName,
    plotVariety = plotVariety,
    type = IncidentType.fromString(type),
    severity = IncidentSeverity.fromString(severity),
    status = IncidentStatus.fromString(status),
    headlineKey = headlineKey,
    metricName = metricName,
    currentValue = currentValue,
    thresholdValue = thresholdValue,
    unit = unit,
    triggeredAt = triggeredAt,
    stressDurationMinutes = stressDurationMinutes,
    snoozedUntil = snoozedUntil,
)

fun SummaryCountsDto.toDomain(): AlertsSummary = AlertsSummary(
    activeCount = activeCount,
    criticalCount = criticalCount,
    warningCount = warningCount,
    normalizedCount = normalizedCount,
)

fun MitigationStepDto.toDomain(): MitigationStep = MitigationStep(
    id = id,
    instructionKey = instructionKey,
    instruction = instruction,
    completed = completed,
    completedAt = completedAt,
)

fun WeeklyTrendPointDto.toDomain(): WeeklyTrendPoint = WeeklyTrendPoint(
    timestamp = timestamp,
    value = value,
    threshold = threshold,
)

fun AgroclimaticIncidentDetailDto.toDomain(): IncidentDetail = IncidentDetail(
    id = id,
    plotId = plotId,
    plotName = plotName,
    plotVariety = plotVariety,
    type = IncidentType.fromString(type),
    severity = IncidentSeverity.fromString(severity),
    status = IncidentStatus.fromString(status),
    headlineKey = headlineKey,
    metricName = metricName,
    currentValue = currentValue,
    thresholdValue = thresholdValue,
    unit = unit,
    triggeredAt = triggeredAt,
    dateFormatted = dateFormatted,
    timeWindow = timeWindow,
    stressDurationMinutes = stressDurationMinutes,
    snoozedUntil = snoozedUntil,
    mitigationSteps = mitigationSteps.map { it.toDomain() },
    weeklyTrend = weeklyTrend.map { it.toDomain() },
)
