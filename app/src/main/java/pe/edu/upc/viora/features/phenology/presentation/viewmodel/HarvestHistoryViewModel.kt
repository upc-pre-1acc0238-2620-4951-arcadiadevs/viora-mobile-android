package pe.edu.upc.viora.features.phenology.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
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
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveBearingIndexUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestLastRefreshUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignChangeKind
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestHistoryUiState
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestNotice
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestPresenter
import pe.edu.upc.viora.features.phenology.presentation.state.PlotSubtitle
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

@HiltViewModel
class HarvestHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeRecords: ObserveHarvestHistoryUseCase,
    observeIndex: ObserveBearingIndexUseCase,
    observeLastRefresh: ObserveHarvestLastRefreshUseCase,
    observePlot: ObservePlotUseCase,
    private val refreshHistory: RefreshHarvestHistoryUseCase,
    private val clock: Clock,
) : ViewModel() {

    val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "HarvestHistoryRoute needs a plotId" }
    private val plotName: String = savedStateHandle.get<String>("plotName").orEmpty()

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private data class Extras(val newRecordId: String? = null, val notice: HarvestNotice? = null)

    private data class Cache(
        val records: List<HarvestRecord>,
        val index: BearingIndex?,
        val lastRefresh: Instant?,
        val plot: Plot?,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private val extras = MutableStateFlow(Extras())

    val uiState: StateFlow<HarvestHistoryUiState> = combine(
        combine(observeRecords(plotId), observeIndex(plotId), observeLastRefresh(plotId), observePlot(PlotId(plotId)), ::Cache),
        refreshState,
        extras,
    ) { cache, refresh, extra -> build(cache, refresh, extra) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HarvestHistoryUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = refreshHistory(plotId)
            refreshState.update {
                RefreshState(
                    isRefreshing = false,
                    hasFinishedOnce = true,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }

    /** The campaign sheet saved or deleted a campaign: confirm it and mark the new row. */
    fun onCampaignChanged(kind: CampaignChangeKind, year: Int, recordId: String?) {
        extras.value = Extras(
            newRecordId = if (kind == CampaignChangeKind.ADDED) recordId else null,
            notice = HarvestNotice(kind, year),
        )
    }

    fun dismissNotice() {
        extras.update { it.copy(notice = null) }
    }

    private fun build(cache: Cache, refresh: RefreshState, extra: Extras): HarvestHistoryUiState {
        val hasCache = cache.records.isNotEmpty() || cache.lastRefresh != null
        if (!hasCache) {
            return when {
                refresh.error != null && !refresh.isRefreshing -> HarvestHistoryUiState.Error(refresh.error)
                refresh.hasFinishedOnce && !refresh.isRefreshing -> content(cache, refresh, extra)
                else -> HarvestHistoryUiState.Loading
            }
        }
        return content(cache, refresh, extra)
    }

    private fun content(cache: Cache, refresh: RefreshState, extra: Extras): HarvestHistoryUiState.Content {
        val records = cache.records.sortedByDescending { it.campaignYear }
        // The server's index is what is shown; the local formula only stands in if it is missing.
        val index = if (records.size >= HoblynBbi.MIN_CAMPAIGNS) {
            cache.index?.value ?: HoblynBbi.index(records)
        } else {
            null
        }
        val bbiClass = index?.let(BbiClass::of)
        val currentYear = LocalDate.now(clock).year
        return HarvestHistoryUiState.Content(
            plotName = plotName.ifBlank { cache.plot?.name.orEmpty() },
            plotSubtitle = cache.plot?.let { PlotSubtitle(it.variety, it.areaHectares) },
            records = records,
            index = index,
            bbiClass = bbiClass,
            intervals = if (index != null) HoblynBbi.intervals(records) else emptyList(),
            averageKg = if (records.isEmpty()) 0.0 else records.sumOf { it.totalYieldKg } / records.size,
            summary = HarvestPresenter.summarize(records, bbiClass, currentYear),
            suggestedYear = HarvestPresenter.suggestedYear(records, currentYear),
            offline = refresh.error is AppError.Offline || refresh.error is AppError.Timeout,
            lastRefresh = cache.lastRefresh,
            isRefreshing = refresh.isRefreshing,
            newRecordId = extra.newRecordId,
            notice = extra.notice,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
