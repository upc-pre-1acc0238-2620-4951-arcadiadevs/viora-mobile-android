package pe.edu.upc.viora.features.phenology.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository
import pe.edu.upc.viora.features.phenology.presentation.state.WinterChillUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

@HiltViewModel
class WinterChillViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeChillTracker: ObserveChillTrackerUseCase,
    observePlot: ObservePlotUseCase,
    private val refreshChillTracker: RefreshChillTrackerUseCase,
    private val chillRepository: ChillRepository,
    private val clock: Clock,
) : ViewModel() {

    val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "WinterChillRoute requires plotId" }
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
    ) { tracker, lastSync, plot, refresh ->
        val plotName = plot?.name ?: initialPlotName.ifEmpty { "Cuartel" }
        val varietyName = plot?.variety?.displayName() ?: "Sevillana"

        val hasCache = tracker != null || lastSync != null
        if (!hasCache) {
            when {
                refresh.error != null && !refresh.isRefreshing -> WinterChillUiState.Error(refresh.error)
                refresh.hasFinishedOnce && !refresh.isRefreshing -> WinterChillUiState.Content(
                    plotName = plotName,
                    varietyName = varietyName,
                    accumulatedPortions = 0,
                    thresholdPortions = 30,
                    daysAbove24Celsius = 0,
                    seasonState = WinterSeasonState.ACCUMULATING,
                    ensoRisk = EnsoRiskLevel.NEUTRAL,
                    projectedCompletionDate = null,
                    previousWinterCompletionDate = null,
                    curvePoints = emptyList(),
                    lastSync = lastSync,
                    isRefreshing = refresh.isRefreshing,
                    offline = refresh.error is AppError.Offline,
                )
                else -> WinterChillUiState.Loading
            }
        } else if (tracker == null) {
            WinterChillUiState.Content(
                plotName = plotName,
                varietyName = varietyName,
                accumulatedPortions = 0,
                thresholdPortions = 30,
                daysAbove24Celsius = 0,
                seasonState = WinterSeasonState.ACCUMULATING,
                ensoRisk = EnsoRiskLevel.NEUTRAL,
                projectedCompletionDate = null,
                previousWinterCompletionDate = null,
                curvePoints = emptyList(),
                lastSync = lastSync,
                isRefreshing = refresh.isRefreshing,
                offline = refresh.error is AppError.Offline,
            )
        } else {
            WinterChillUiState.Content(
                plotName = plotName,
                varietyName = varietyName,
                accumulatedPortions = tracker.accumulatedPortions.roundToInt(),
                thresholdPortions = tracker.thresholdPortions.roundToInt().coerceAtLeast(30),
                daysAbove24Celsius = tracker.daysAbove24Celsius,
                seasonState = tracker.seasonState,
                ensoRisk = tracker.ensoRisk,
                projectedCompletionDate = tracker.projectedCompletionDate,
                previousWinterCompletionDate = tracker.previousWinterCompletionDate,
                curvePoints = tracker.curvePoints,
                lastSync = lastSync ?: tracker.syncedAt,
                isRefreshing = refresh.isRefreshing,
                offline = refresh.error is AppError.Offline,
            )
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
