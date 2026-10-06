package pe.edu.upc.viora.features.climate.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.climate.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.climate.domain.repository.WeatherForecastRepository

class ObserveWeatherForecastUseCase @Inject constructor(
    private val repository: WeatherForecastRepository,
) {
    operator fun invoke(plotId: String): Flow<WeatherForecast?> =
        repository.observeForecast(plotId)
}
