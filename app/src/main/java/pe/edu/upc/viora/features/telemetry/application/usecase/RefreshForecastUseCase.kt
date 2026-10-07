package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.repository.ForecastRepository

/** Downloads the 7-day forecast of a plot into the local cache (US19). */
class RefreshForecastUseCase @Inject constructor(
    private val repository: ForecastRepository,
) {
    suspend operator fun invoke(plotId: String): AppResult<Unit> = repository.refresh(plotId)
}
