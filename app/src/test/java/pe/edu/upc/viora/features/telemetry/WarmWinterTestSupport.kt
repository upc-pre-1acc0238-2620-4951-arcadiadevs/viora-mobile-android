package pe.edu.upc.viora.features.telemetry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail
import pe.edu.upc.viora.features.telemetry.domain.entity.WeeklyTrendPoint
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

/** Incidents cached on the phone and one detail answer, for the warm winter tests (T15, P80). */
internal class FakeWarmWinterIncidents : IncidentRepository {
    val incidents = MutableStateFlow<List<AgroclimaticIncident>>(emptyList())
    var detail: AppResult<IncidentDetail> = AppResult.Failure(AppError.Offline)

    override fun observeIncidents(plotId: String?): Flow<List<AgroclimaticIncident>> =
        incidents.map { list -> list.filter { plotId == null || it.plotId == plotId } }

    override suspend fun refresh(plotId: String?, status: String?, severity: String?): AppResult<AlertsSummary> =
        AppResult.Success(AlertsSummary(0, 0, 0, 0))

    override suspend fun getIncidentDetail(incidentId: String): AppResult<IncidentDetail> = detail

    override suspend fun postponeIncident(incidentId: String, durationHours: Int): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun completeMitigationStep(incidentId: String, stepId: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun getSummary(): AppResult<AlertsSummary> = AppResult.Success(AlertsSummary(0, 0, 0, 0))
}

internal fun warmWinterIncident(
    id: String = "ww-1",
    plotId: String = "plot-1",
    status: IncidentStatus = IncidentStatus.ACTIVE,
    type: IncidentType = IncidentType.WARM_WINTER,
) = AgroclimaticIncident(
    id = id,
    plotId = plotId,
    plotName = "La Yarada 02",
    plotVariety = "SEVILLANA",
    type = type,
    severity = IncidentSeverity.CRITICAL,
    status = status,
    headlineKey = "warm_winter.headline",
    metricName = "DAILY_MAX_TEMPERATURE",
    currentValue = 25.0,
    thresholdValue = 24.0,
    unit = "°C",
    triggeredAt = "2026-07-19T11:00:00Z",
    stressDurationMinutes = 0,
)

/** Daily maxima of the week up to the trigger, oldest first: Mon 22 °C, then 25, 26, 27, 25 (four warm days). */
internal fun warmWinterDetail(maxima: List<Double> = listOf(22.0, 25.0, 26.0, 27.0, 25.0)) = IncidentDetail(
    id = "ww-1",
    plotId = "plot-1",
    plotName = "La Yarada 02",
    plotVariety = "SEVILLANA",
    type = IncidentType.WARM_WINTER,
    severity = IncidentSeverity.CRITICAL,
    status = IncidentStatus.ACTIVE,
    headlineKey = "warm_winter.headline",
    metricName = "DAILY_MAX_TEMPERATURE",
    currentValue = maxima.last(),
    thresholdValue = 24.0,
    unit = "°C",
    triggeredAt = "2026-07-19T11:00:00Z",
    dateFormatted = "",
    timeWindow = "",
    stressDurationMinutes = 0,
    weeklyTrend = maxima.mapIndexed { day, value ->
        WeeklyTrendPoint(timestamp = "2026-07-${14 + day}T05:00:00Z", value = value, threshold = 24.0)
    },
)
