package pe.edu.upc.viora.features.telemetry.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail

/**
 * Offline-first access to agroclimatic incidents and alerts.
 * The Room database acts as the single source of truth, observed by screens and synced via [refresh].
 */
interface IncidentRepository {

    /** Emits the cached incidents, optionally filtered by [plotId], ordered by detection time descending. */
    fun observeIncidents(plotId: String? = null): Flow<List<AgroclimaticIncident>>

    /**
     * When the incidents of [plotId] (or of every plot, with null) were last downloaded, in epoch
     * millis; null while they never were. Lets a screen tell "no alerts" from "not loaded yet".
     */
    fun observeLastRefresh(plotId: String? = null): Flow<Long?> = flowOf(null)

    /** Refreshes incidents from the backend API and updates the local Room database cache. */
    suspend fun refresh(plotId: String? = null, status: String? = null, severity: String? = null): AppResult<AlertsSummary>

    /** Retrieves the full incident details including weekly trend and actionable checklist (US18, T15). */
    suspend fun getIncidentDetail(incidentId: String): AppResult<IncidentDetail>

    /** Postpones (snoozes) notifications for the incident by [durationHours]. */
    suspend fun postponeIncident(incidentId: String, durationHours: Int): AppResult<Unit>

    /** Marks a specific mitigation task step within an incident as completed. */
    suspend fun completeMitigationStep(incidentId: String, stepId: String): AppResult<Unit>

    /** Retrieves the latest quantitative summary counts for the alerts bell and badge. */
    suspend fun getSummary(): AppResult<AlertsSummary>
}
