package pe.edu.upc.viora.features.climate.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.climate.domain.repository.WeatherForecastRepository

class RefreshWeatherForecastUseCase @Inject constructor(
    private val repository: WeatherForecastRepository,
) {
    suspend operator fun invoke(plotId: String): AppResult<Unit> =
        repository.refreshForecast(plotId)
}
