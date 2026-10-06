package pe.edu.upc.viora.features.climate.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.climate.application.usecase.ObserveForecastLastRefreshUseCase
import pe.edu.upc.viora.features.climate.application.usecase.ObserveWeatherForecastUseCase
import pe.edu.upc.viora.features.climate.application.usecase.RefreshWeatherForecastUseCase
import pe.edu.upc.viora.features.climate.presentation.state.ClimateUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

@HiltViewModel
class ClimateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeWeatherForecast: ObserveWeatherForecastUseCase,
    observeLastRefresh: ObserveForecastLastRefreshUseCase,
    observePlot: ObservePlotUseCase,
    private val refreshWeatherForecast: RefreshWeatherForecastUseCase,
) : ViewModel() {

    val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "ClimateRoute requires plotId" }
    private val initialPlotName: String = savedStateHandle.get<String>("plotName").orEmpty()

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private val selectedDayIndex = MutableStateFlow(0)

    val uiState: StateFlow<ClimateUiState> = combine(
        observePlot(PlotId(plotId)),
        observeWeatherForecast(plotId),
        observeLastRefresh(plotId),
        selectedDayIndex,
        refreshState,
    ) { plot, forecast, lastRefresh, dayIndex, refresh ->
        val dailyForecasts = forecast?.dailyForecasts.orEmpty()
        val offline = refresh.error is AppError.Offline || (refresh.error != null && dailyForecasts.isNotEmpty())

        when {
            dailyForecasts.isNotEmpty() -> ClimateUiState.Content(
                plotId = plotId,
                plotName = plot?.name ?: initialPlotName,
                variety = plot?.variety,
                areaHectares = plot?.areaHectares,
                estimatedTrees = plot?.estimatedTrees,
                dailyForecasts = dailyForecasts,
                selectedDayIndex = dayIndex.coerceIn(0, dailyForecasts.lastIndex),
                isOffline = offline,
                lastRefresh = lastRefresh,
                isRefreshing = refresh.isRefreshing,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> ClimateUiState.Loading
            refresh.error != null -> ClimateUiState.Error(refresh.error)
            else -> ClimateUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ClimateUiState.Loading)

    init {
        refresh()
    }

    fun selectDay(index: Int) {
        selectedDayIndex.value = index
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = refreshWeatherForecast(plotId)
            refreshState.update {
                RefreshState(
                    isRefreshing = false,
                    hasFinishedOnce = true,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }
}
