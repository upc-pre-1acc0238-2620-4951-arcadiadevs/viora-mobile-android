package pe.edu.upc.viora.features.climate.application.usecase

import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.climate.domain.repository.WeatherForecastRepository

class ObserveForecastLastRefreshUseCase @Inject constructor(
    private val repository: WeatherForecastRepository,
) {
    operator fun invoke(plotId: String): Flow<Instant?> =
        repository.observeLastRefresh(plotId)
}
