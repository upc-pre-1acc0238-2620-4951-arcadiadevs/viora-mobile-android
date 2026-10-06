package pe.edu.upc.viora.features.climate.presentation.state

import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.climate.domain.entity.DayForecast
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

sealed interface ClimateUiState {
    data object Loading : ClimateUiState
    data class Error(val error: AppError) : ClimateUiState
    data class Content(
        val plotId: String,
        val plotName: String,
        val variety: OliveVariety?,
        val areaHectares: Double?,
        val estimatedTrees: Int?,
        val dailyForecasts: List<DayForecast>,
        val selectedDayIndex: Int = 0,
        val isOffline: Boolean = false,
        val lastRefresh: Instant? = null,
        val isRefreshing: Boolean = false,
    ) : ClimateUiState {
        val selectedDay: DayForecast? get() = dailyForecasts.getOrNull(selectedDayIndex) ?: dailyForecasts.firstOrNull()
        val hasFrostAlert: Boolean get() = dailyForecasts.any { it.isFrostRisk }
    }
}
