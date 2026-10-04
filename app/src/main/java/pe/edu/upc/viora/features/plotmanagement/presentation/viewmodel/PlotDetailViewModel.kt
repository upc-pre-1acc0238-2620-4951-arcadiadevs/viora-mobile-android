package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveBearingIndexUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestLastRefreshUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ArchivePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.ArchiveState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.LotHarvest
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotDetailUiState

@HiltViewModel
class PlotDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlot: ObservePlotUseCase,
    observePlots: ObservePlotsUseCase,
    private val archivePlot: ArchivePlotUseCase,
    observeHarvests: ObserveHarvestHistoryUseCase,
    observeBearingIndex: ObserveBearingIndexUseCase,
    observeHarvestRefresh: ObserveHarvestLastRefreshUseCase,
    private val refreshHarvests: RefreshHarvestHistoryUseCase,
) : ViewModel() {

    // Navigation stores each property of PlotDetailRoute in the saved state under its own name.
    // They are read by name (not with toRoute) so the view model can be tested on the JVM.
    private val plotId = PlotId(checkNotNull(savedStateHandle.get<String>(KEY_PLOT_ID)) { "PlotDetailRoute needs a plotId" })

    // Kept in the saved state so the notice does not come back after the process is recreated.
    private val savedNoticeVisible: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(KEY_NOTICE_VISIBLE, savedStateHandle.get<Boolean>(KEY_JUST_SAVED) ?: false)

    private val harvest: Flow<LotHarvest?> = combine(
        observeHarvests(plotId.value),
        observeBearingIndex(plotId.value),
        observeHarvestRefresh(plotId.value),
    ) { records, index, lastRefresh ->
        when {
            records.isEmpty() && lastRefresh == null -> null
            records.size >= HoblynBbi.MIN_CAMPAIGNS -> LotHarvest(index?.value ?: HoblynBbi.index(records), missingCampaigns = 0)
            else -> LotHarvest(index = null, missingCampaigns = HoblynBbi.MIN_CAMPAIGNS - records.size)
        }
    }

    val uiState: StateFlow<PlotDetailUiState> = combine(
        observePlot(plotId),
        observePlots(),
        savedNoticeVisible,
        harvest,
    ) { plot, activePlots, noticeVisible, lotHarvest ->
        if (plot == null) {
            PlotDetailUiState.NotFound
        } else {
            PlotDetailUiState.Content(
                plot,
                noticeVisible,
                activeHectares = activePlots.sumOf { it.areaHectares },
                harvest = lotHarvest,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlotDetailUiState.Loading)

    private val _archiveState = MutableStateFlow(ArchiveState.Idle)
    val archiveState: StateFlow<ArchiveState> = _archiveState.asStateFlow()

    /** Archives this plot. When it works [archiveState] becomes [ArchiveState.Done] and the screen leaves. */
    fun archive() {
        if (_archiveState.value == ArchiveState.Working) return
        _archiveState.value = ArchiveState.Working
        viewModelScope.launch {
            _archiveState.value = when (archivePlot(plotId)) {
                is AppResult.Success -> ArchiveState.Done
                is AppResult.Failure -> ArchiveState.Failed
            }
        }
    }

    fun dismissArchiveFailure() {
        if (_archiveState.value == ArchiveState.Failed) _archiveState.value = ArchiveState.Idle
    }

    init {
        // Best effort: the alternation card falls back to a generic line if this cannot be downloaded.
        viewModelScope.launch { refreshHarvests(plotId.value) }
        if (savedNoticeVisible.value) {
            viewModelScope.launch {
                delay(SAVED_NOTICE_MILLIS)
                savedStateHandle[KEY_NOTICE_VISIBLE] = false
            }
        }
    }

    private companion object {
        const val KEY_PLOT_ID = "plotId"
        const val KEY_JUST_SAVED = "justSaved"
        const val KEY_NOTICE_VISIBLE = "savedNoticeVisible"
        const val SAVED_NOTICE_MILLIS = 3_000L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
