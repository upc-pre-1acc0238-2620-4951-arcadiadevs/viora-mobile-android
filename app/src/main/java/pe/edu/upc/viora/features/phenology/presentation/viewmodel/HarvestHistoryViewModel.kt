package pe.edu.upc.viora.features.phenology.presentation.viewmodel

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
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveAlternationSummaryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.domain.AlternationSummary
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignChangeKind
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestHistoryUiState
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestNotice
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestPresenter
import pe.edu.upc.viora.features.phenology.presentation.state.PlotSubtitle
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.published.Plot
import pe.edu.upc.viora.core.presentation.requirePlotId

@HiltViewModel
class HarvestHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeAlternation: ObserveAlternationSummaryUseCase,
    observePlot: ObservePlotUseCase,
    private val refreshHistory: RefreshHarvestHistoryUseCase,
    private val clock: Clock,
) : ViewModel() {

    val plotId: String = savedStateHandle.requirePlotId().value
    private val plotName: String = savedStateHandle.get<String>("plotName").orEmpty()

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private data class Extras(val newRecordId: String? = null, val notice: HarvestNotice? = null)

    /** The campaigns and the alternation as every screen reads them; null while nothing was ever cached. */
    private data class Cache(val alternation: AlternationSummary?, val plot: Plot?)

    private val refreshState = MutableStateFlow(RefreshState())
    private val extras = MutableStateFlow(Extras())

    val uiState: StateFlow<HarvestHistoryUiState> = combine(
        combine(observeAlternation(plotId), observePlot(PlotId(plotId)), ::Cache),
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
        if (cache.alternation == null) {
            return when {
                refresh.error != null && !refresh.isRefreshing -> HarvestHistoryUiState.Error(refresh.error)
                refresh.hasFinishedOnce && !refresh.isRefreshing -> content(cache, refresh, extra)
                else -> HarvestHistoryUiState.Loading
            }
        }
        return content(cache, refresh, extra)
    }

    private fun content(cache: Cache, refresh: RefreshState, extra: Extras): HarvestHistoryUiState.Content {
        val records = cache.alternation?.records.orEmpty().sortedByDescending { it.campaignYear }
        // Phenology decides the index once for every screen (the server's, or the local formula while it is missing).
        val index = cache.alternation?.index
        val bbiClass = cache.alternation?.bbiClass
        val currentYear = LocalDate.now(clock).year
        val summary = HarvestPresenter.summarize(records, bbiClass, currentYear)
        val suggestedYear = HarvestPresenter.suggestedYear(records, currentYear)
        return HarvestHistoryUiState.Content(
            plotName = plotName.ifBlank { cache.plot?.name.orEmpty() },
            plotSubtitle = cache.plot?.let { PlotSubtitle(it.variety, it.areaHectares, it.estimatedTrees) },
            records = records,
            index = index,
            bbiClass = bbiClass,
            intervals = if (index != null) HoblynBbi.intervals(records) else emptyList(),
            averageKg = if (records.isEmpty()) 0.0 else records.sumOf { it.totalYieldKg } / records.size,
            areaHectares = cache.plot?.areaHectares?.takeIf { it > 0 },
            missingYears = HarvestPresenter.missingYears(summary.missing, suggestedYear),
            summary = summary,
            suggestedYear = suggestedYear,
            offline = refresh.error.isConnectivity,
            lastRefresh = cache.alternation?.lastRefresh,
            isRefreshing = refresh.isRefreshing,
            newRecordId = extra.newRecordId,
            notice = extra.notice,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
