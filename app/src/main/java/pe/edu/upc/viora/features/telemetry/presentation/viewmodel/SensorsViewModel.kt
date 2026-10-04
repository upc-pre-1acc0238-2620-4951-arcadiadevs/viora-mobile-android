package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveSensorNodesUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.RefreshSensorNodesUseCase
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.features.telemetry.presentation.state.SensorsUiState

@HiltViewModel
class SensorsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeNodes: ObserveSensorNodesUseCase,
    observePlot: ObservePlotUseCase,
    private val refreshNodes: RefreshSensorNodesUseCase,
) : ViewModel() {

    val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "SensorsRoute needs a plotId" }
    private val plotName: String = savedStateHandle.get<String>("plotName").orEmpty()

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<SensorsUiState> = combine(
        observeNodes(plotId),
        observePlot(PlotId(plotId)),
        refreshState,
    ) { nodes, plot, refresh ->
        val resolvedName = plotName.ifBlank { plot?.name ?: "" }
        when {
            nodes.isNotEmpty() -> SensorsUiState.Content(
                plotName = resolvedName,
                nodes = nodes,
                isRefreshing = refresh.isRefreshing,
                refreshError = refresh.error,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> SensorsUiState.Loading
            refresh.error != null -> SensorsUiState.Error(refresh.error)
            else -> SensorsUiState.Empty(plotName = resolvedName)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SensorsUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = refreshNodes(plotId)
            refreshState.update {
                RefreshState(
                    isRefreshing = false,
                    hasFinishedOnce = true,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
