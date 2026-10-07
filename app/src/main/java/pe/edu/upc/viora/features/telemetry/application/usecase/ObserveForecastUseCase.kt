package pe.edu.upc.viora.features.telemetry.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.telemetry.domain.repository.ForecastRepository

/** Observes the cached 7-day forecast of a plot (US19). */
class ObserveForecastUseCase @Inject constructor(
    private val repository: ForecastRepository,
) {
    operator fun invoke(plotId: String): Flow<WeatherForecast?> = repository.observeForecast(plotId)
}
