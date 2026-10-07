package pe.edu.upc.viora.features.telemetry.application.usecase

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.telemetry.domain.repository.TelemetryRepository

/** When the series of a plot was last downloaded, so the UI never presents cached data as current. */
class ObserveTelemetryLastRefreshUseCase @Inject constructor(
    private val repository: TelemetryRepository,
) {
    operator fun invoke(plotId: String): Flow<Instant?> = repository.observeLastRefresh(plotId)
}
