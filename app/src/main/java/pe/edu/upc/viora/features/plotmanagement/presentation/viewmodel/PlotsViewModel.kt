package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveArchivedPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshArchivedPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RestorePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotsFilter
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotsUiState

@HiltViewModel
class PlotsViewModel @Inject constructor(
    observePlots: ObservePlotsUseCase,
    observeArchivedPlots: ObserveArchivedPlotsUseCase,
    observeLastRefresh: ObservePlotsLastRefreshUseCase,
    private val refreshPlots: RefreshPlotsUseCase,
    private val refreshArchivedPlots: RefreshArchivedPlotsUseCase,
    private val restorePlot: RestorePlotUseCase,
) : ViewModel() {

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    /** What the producer chose on the list: the filter and the restoration in progress. */
    private data class ChoiceState(
        val filter: PlotsFilter = PlotsFilter.ACTIVE,
        val restoringId: PlotId? = null,
        val restoreError: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private val choiceState = MutableStateFlow(ChoiceState())

    private val plotLists = combine(observePlots(), observeArchivedPlots()) { active, archived -> active to archived }

    val uiState: StateFlow<PlotsUiState> = combine(
        plotLists,
        observeLastRefresh(),
        refreshState,
        choiceState,
    ) { (plots, archived), lastRefresh, refresh, choice ->
        when {
            plots.isNotEmpty() || archived.isNotEmpty() -> PlotsUiState.Content(
                plots = plots,
                isRefreshing = refresh.isRefreshing,
                refreshError = refresh.error,
                lastRefresh = lastRefresh,
                archivedPlots = archived,
                // With nothing archived (e.g. the last one was just restored) the list is the active one.
                filter = if (choice.filter == PlotsFilter.ARCHIVED && archived.isNotEmpty()) PlotsFilter.ARCHIVED else PlotsFilter.ACTIVE,
                restoringId = choice.restoringId,
                restoreError = choice.restoreError,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> PlotsUiState.Loading
            refresh.error != null -> PlotsUiState.Error(refresh.error)
            else -> PlotsUiState.Empty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlotsUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = refreshPlots()
            // The archived ones are a secondary list: if they cannot be read the active ones are still shown.
            refreshArchivedPlots()
            refreshState.update {
                RefreshState(
                    isRefreshing = false,
                    hasFinishedOnce = true,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }

    fun selectFilter(filter: PlotsFilter) {
        choiceState.update { it.copy(filter = filter, restoreError = null) }
    }

    /** Brings an archived plot back. The cache takes it from the archived list to the active one. */
    fun restore(id: PlotId) {
        if (choiceState.value.restoringId != null) return
        choiceState.update { it.copy(restoringId = id, restoreError = null) }
        viewModelScope.launch {
            val error = (restorePlot(id) as? AppResult.Failure)?.error
            choiceState.update { it.copy(restoringId = null, restoreError = error) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
