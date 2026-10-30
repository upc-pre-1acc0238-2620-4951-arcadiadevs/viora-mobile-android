package pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
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
import pe.edu.upc.viora.core.domain.PlotId
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningExecutionUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RefreshThinningExecutionUseCase
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ThinningResultUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.core.presentation.requirePlotId

/**
 * P63 · Aclareo registrado. Right after P62 the thinning is already cached; opened later from P61 it is downloaded
 * again, and without signal the cached one is shown.
 */
@HiltViewModel
class ThinningResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeExecution: ObserveThinningExecutionUseCase,
    observePlot: ObservePlotUseCase,
    private val refreshExecution: RefreshThinningExecutionUseCase,
    clock: Clock,
) : ViewModel() {

    val plotId: String = savedStateHandle.requirePlotId().value
    private val prescriptionId: String = savedStateHandle.get<String>("prescriptionId").orEmpty()
    private val initialPlotName: String = savedStateHandle.get<String>("plotName").orEmpty()
    val campaignYear: Int = savedStateHandle.get<Int>("campaignYear")?.takeIf { it > 0 } ?: LocalDate.now(clock).year

    private data class RefreshState(val isRefreshing: Boolean = false, val error: AppError? = null)

    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<ThinningResultUiState> = combine(
        observeExecution(plotId, campaignYear),
        observePlot(PlotId(plotId)),
        refreshState,
    ) { execution, plot, refresh ->
        when {
            execution != null -> ThinningResultUiState.Content(
                plotName = plot?.name ?: initialPlotName,
                variety = plot?.variety,
                campaignYear = campaignYear,
                execution = execution,
            )
            refresh.isRefreshing -> ThinningResultUiState.Loading
            // Downloaded and still nothing: the thinning was never recorded for this plan.
            else -> ThinningResultUiState.Error(refresh.error ?: AppError.NotFound())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThinningResultUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        if (prescriptionId.isBlank() || refreshState.value.isRefreshing) return
        refreshState.update { RefreshState(isRefreshing = true) }
        viewModelScope.launch {
            val result = refreshExecution(plotId, campaignYear, prescriptionId)
            refreshState.update { RefreshState(isRefreshing = false, error = (result as? AppResult.Failure)?.error) }
        }
    }
}
