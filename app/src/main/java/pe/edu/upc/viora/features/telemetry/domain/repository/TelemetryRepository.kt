package pe.edu.upc.viora.features.telemetry.domain.repository

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.HourlyReading
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange

interface TelemetryRepository {
    /** Emits the cached hourly readings of the plot since [since], oldest first. */
    fun observeReadings(plotId: String, since: Instant): Flow<List<HourlyReading>>

    /** When the readings of the plot were last downloaded, or null if never. */
    fun observeLastRefresh(plotId: String): Flow<Instant?>

    /** Downloads the readings of [range] ending now and stores them in the local cache (US17). */
    suspend fun refresh(plotId: String, range: TelemetryRange): AppResult<Unit>
}
