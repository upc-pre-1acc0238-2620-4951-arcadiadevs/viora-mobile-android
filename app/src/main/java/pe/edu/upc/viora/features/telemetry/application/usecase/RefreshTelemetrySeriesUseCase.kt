package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.repository.TelemetryRepository
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange

/** Downloads the hourly series of a plot into the local cache (US17). */
class RefreshTelemetrySeriesUseCase @Inject constructor(
    private val repository: TelemetryRepository,
) {
    suspend operator fun invoke(plotId: String, range: TelemetryRange): AppResult<Unit> =
        repository.refresh(plotId, range)
}
