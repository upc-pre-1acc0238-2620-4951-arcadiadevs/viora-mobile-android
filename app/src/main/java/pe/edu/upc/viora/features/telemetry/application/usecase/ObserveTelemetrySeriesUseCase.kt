package pe.edu.upc.viora.features.telemetry.application.usecase

import java.time.Clock
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.telemetry.domain.entity.TelemetrySeries
import pe.edu.upc.viora.features.telemetry.domain.repository.TelemetryRepository
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange

/** Observes the cached hourly series of a plot for the chosen [TelemetryRange] (US17). */
class ObserveTelemetrySeriesUseCase @Inject constructor(
    private val repository: TelemetryRepository,
    private val clock: Clock,
) {
    operator fun invoke(plotId: String, range: TelemetryRange): Flow<TelemetrySeries> {
        val since = clock.instant().minus(Duration.ofHours(range.hours))
        return repository.observeReadings(plotId, since).map { TelemetrySeries(plotId, it) }
    }
}
