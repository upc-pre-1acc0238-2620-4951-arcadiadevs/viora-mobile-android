package pe.edu.upc.viora.features.telemetry.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail

interface IncidentRepository {
    fun observeIncidents(plotId: String? = null): Flow<List<AgroclimaticIncident>>
    suspend fun refresh(plotId: String? = null, status: String? = null, severity: String? = null): AppResult<AlertsSummary>
    suspend fun getIncidentDetail(incidentId: String): AppResult<IncidentDetail>
    suspend fun postponeIncident(incidentId: String, durationHours: Int): AppResult<Unit>
    suspend fun completeMitigationStep(incidentId: String, stepId: String): AppResult<Unit>
    suspend fun getSummary(): AppResult<AlertsSummary>
}
