package pe.edu.upc.viora.features.phenology.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.OliveVariety
import pe.edu.upc.viora.core.domain.PlotId
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository
import pe.edu.upc.viora.features.phenology.presentation.state.WinterChillUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.application.published.IncidentType
import pe.edu.upc.viora.core.presentation.requirePlotId

@HiltViewModel
class WinterChillViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeChillTracker: ObserveChillTrackerUseCase,
    observePlot: ObservePlotUseCase,
    private val refreshChillTracker: RefreshChillTrackerUseCase,
    private val chillRepository: ChillRepository,
    observeIncidents: ObserveIncidentsUseCase,
) : ViewModel() {

    val plotId: String = savedStateHandle.requirePlotId().value
    private val initialPlotName: String = savedStateHandle.get<String>("plotName").orEmpty()

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<WinterChillUiState> = combine(
        observeChillTracker(plotId),
        chillRepository.observeLastSync(plotId),
        observePlot(PlotId(plotId)),
        refreshState,
        observeIncidents(plotId).map { incidents ->
            incidents.firstOrNull { it.type == IncidentType.WARM_WINTER && it.status.isActive }?.id
        },
    ) { tracker, lastSync, plot, refresh, warmWinterAlertId ->
        val plotName = plot?.name ?: initialPlotName
        val varietyName = plot?.variety?.displayName().orEmpty()

        when {
            tracker != null -> WinterChillUiState.Content(
                plotName = plotName,
                varietyName = varietyName,
                tracker = tracker,
                lastSync = lastSync ?: tracker.syncedAt,
                isRefreshing = refresh.isRefreshing,
                offline = refresh.error?.isConnectivity == true,
                warmWinterAlertId = warmWinterAlertId,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> WinterChillUiState.Loading
            refresh.error != null -> WinterChillUiState.Error(refresh.error)
            else -> WinterChillUiState.Empty(plotName = plotName, varietyName = varietyName)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WinterChillUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            val result = refreshChillTracker(plotId)
            refreshState.update { current ->
                current.copy(
                    isRefreshing = false,
                    hasFinishedOnce = true,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }
}

private fun OliveVariety.displayName(): String = when (this) {
    OliveVariety.CRIOLLA -> "Criolla"
    OliveVariety.SEVILLANA -> "Sevillana"
    OliveVariety.MANZANILLA -> "Manzanilla"
    OliveVariety.ARBEQUINA -> "Arbequina"
}
