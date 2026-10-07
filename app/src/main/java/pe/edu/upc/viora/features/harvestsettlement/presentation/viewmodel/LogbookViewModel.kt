package pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.onSuccess
import pe.edu.upc.viora.features.croploadregulation.application.usecase.GetPlotSamplingOverviewUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.GetThinningEventsUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveActiveSamplingUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObservePendingDraftSamplesCountUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningEventsUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningEventType
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.ActiveSamplingUiModel
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookFilter
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookGroup
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookPeriod
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookRowType
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettledHarvestEntry
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

/**
 * Logbook (P50): shows settled harvest campaigns, field samplings, and thinning events.
 * Detects real in-progress samplings from infrastructure.
 */
@HiltViewModel
class LogbookViewModel @Inject constructor(
    observeSettlements: ObserveSettledHarvestsUseCase,
    private val observePlots: ObservePlotsUseCase,
    observePlotsLastRefresh: ObservePlotsLastRefreshUseCase,
    private val refreshPlots: RefreshPlotsUseCase,
    private val refreshSettlements: RefreshSettledHarvestsUseCase,
    private val clock: Clock,
    private val getPlotSamplingOverview: GetPlotSamplingOverviewUseCase? = null,
    private val getThinningEvents: GetThinningEventsUseCase? = null,
    observeActiveSampling: ObserveActiveSamplingUseCase? = null,
    observePendingDraftSamplesCount: ObservePendingDraftSamplesCountUseCase? = null,
    observeThinningEvents: ObserveThinningEventsUseCase? = null,
) : ViewModel() {

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private data class LocalState(
        val refresh: RefreshState = RefreshState(),
        val filter: LogbookFilter = LogbookFilter.ALL,
        val activeSampling: ActiveSamplingUiModel? = null,
        val thinningEvents: List<ThinningEvent>? = null,
        val pendingCount: Int = 0,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private val filter = MutableStateFlow(LogbookFilter.ALL)
    private val activeSamplingState = MutableStateFlow<ActiveSamplingUiModel?>(null)

    /** Read from the Room cache, so the timeline is drawn before the network answers. */
    private val thinningEvents: Flow<List<ThinningEvent>?> = observeThinningEvents?.invoke() ?: flowOf(null)

    private val draftSamplingFlow = combine(
        observeActiveSampling?.invoke() ?: flowOf(null),
        observePendingDraftSamplesCount?.invoke() ?: flowOf(0),
    ) { activeDraft, pendingCount ->
        activeDraft to pendingCount
    }

    private val localState = combine(
        refreshState,
        filter,
        activeSamplingState,
        thinningEvents,
        draftSamplingFlow,
    ) { refresh, selectedFilter, activeServer, events, draftInfo ->
        val (activeDraft, pendingCount) = draftInfo
        val completedPlotIds = events.orEmpty()
            .filter { it.eventType == ThinningEventType.SAMPLING_COMPLETED }
            .map { it.plotId }
            .toSet()

        val candidate = activeDraft?.let {
            val total = if (it.treesNeeded > 0) it.sampledTreesCount + it.treesNeeded else it.sampledTreesCount
            ActiveSamplingUiModel(
                plotId = it.plotId,
                plotName = it.plotName,
                completedTrees = it.sampledTreesCount,
                targetTrees = total.coerceAtLeast(1),
            )
        } ?: activeServer

        val active = if (candidate != null &&
            !completedPlotIds.contains(candidate.plotId) &&
            candidate.completedTrees < candidate.targetTrees
        ) {
            candidate
        } else {
            null
        }

        LocalState(refresh, selectedFilter, active, events, pendingCount)
    }

    val uiState: StateFlow<LogbookUiState> = combine(
        observeSettlements(),
        observePlots(),
        observePlotsLastRefresh(),
        localState,
    ) { settlements, plots, lastPlotRefresh, local ->
        val (refresh, selected, activeSampling, thinningEvents, pendingCount) = local
        // A timeline downloaded before (even an empty one) is drawn at once; only a first visit waits.
        if (settlements.isEmpty() && thinningEvents == null && activeSampling == null && !refresh.hasFinishedOnce) {
            LogbookUiState.Loading
        } else {
            LogbookUiState.Content(
                filter = selected,
                groups = groupsFor(selected, settlements, plots, thinningEvents.orEmpty()),
                activeSampling = activeSampling,
                pendingLocalCount = pendingCount,
                refreshError = refresh.error,
                lastPlotRefresh = lastPlotRefresh,
                isRefreshing = refresh.isRefreshing,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LogbookUiState.Loading)

    init {
        refresh()
    }

    fun selectFilter(selected: LogbookFilter) {
        filter.value = selected
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val result = refreshAll()
            refreshState.update {
                RefreshState(
                    isRefreshing = false,
                    hasFinishedOnce = true,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }

    /**
     * The three feeds of the logbook are independent, so they are downloaded at the same time (one
     * after the other they took ~9 s on Render). The plot ids come from the cache; with nothing
     * cached yet the plots are downloaded first, and only the settlements wait for them.
     */
    private suspend fun refreshAll(): AppResult<Unit> = coroutineScope {
        launch { refreshActiveSampling() }
        // The answer fills the Room cache that [thinningEvents] observes.
        launch { getThinningEvents?.invoke(campaignYear = null, plotId = null) }
        refreshSettlementsOfCachedPlots()
    }

    private suspend fun refreshSettlementsOfCachedPlots(): AppResult<Unit> {
        var plots = observePlots().first()
        if (plots.isEmpty()) {
            val plotsResult = refreshPlots()
            if (plotsResult is AppResult.Failure) return plotsResult
            plots = observePlots().first()
        }
        return refreshSettlements(plots.map { it.id.value })
    }

    /** The server's sampling in progress, shown when the phone has no draft of its own. */
    private suspend fun refreshActiveSampling() {
        getPlotSamplingOverview?.invoke(campaignYear = null)?.onSuccess { overviews ->
            val inProgress = overviews.firstOrNull {
                it.samplingStatus == SamplingStatus.IN_PROGRESS &&
                    it.treesNeeded > 0 &&
                    it.sampledTreesCount < 5
            }
            activeSamplingState.value = inProgress?.let {
                val total = if (it.treesNeeded > 0) it.sampledTreesCount + it.treesNeeded else it.sampledTreesCount
                ActiveSamplingUiModel(
                    plotId = it.plotId,
                    plotName = it.plotName,
                    completedTrees = it.sampledTreesCount,
                    targetTrees = total.coerceAtLeast(1),
                )
            }
        }
    }

    private fun groupsFor(
        selected: LogbookFilter,
        settlements: List<HarvestSettlement>,
        plots: List<Plot>,
        thinningEvents: List<ThinningEvent>,
    ): List<LogbookGroup> {
        val names = plots.associate { it.id.value to it.name }
        val today = LocalDate.now(clock)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val month = YearMonth.from(today)

        val harvestEntries = settlements.map {
            SettledHarvestEntry(
                id = it.id,
                plotId = it.plotId,
                plotName = names[it.plotId],
                campaignYear = it.campaignYear,
                totalYieldKg = it.totalYieldKg,
                status = it.status,
                settledAt = it.settledAt,
                type = LogbookRowType.HARVEST,
            )
        }

        val samplingEntries = thinningEvents
            .filter { it.eventType == ThinningEventType.SAMPLING_COMPLETED }
            .map {
                SettledHarvestEntry(
                    id = it.id,
                    plotId = it.plotId,
                    plotName = it.plotName.ifBlank { names[it.plotId] },
                    campaignYear = it.campaignYear,
                    settledAt = it.occurredAt,
                    type = LogbookRowType.SAMPLING,
                    evaluatedTreesCount = it.evaluatedTreesCount,
                    meanFruitsPerShoot = it.meanFruitsPerShoot,
                )
            }

        val thinningEntries = thinningEvents
            .filter { it.eventType == ThinningEventType.THINNING_EXECUTED }
            .map {
                SettledHarvestEntry(
                    id = it.id,
                    plotId = it.plotId,
                    plotName = it.plotName.ifBlank { names[it.plotId] },
                    campaignYear = it.campaignYear,
                    settledAt = it.occurredAt,
                    type = LogbookRowType.THINNING,
                    removalPercentage = it.removalPercentage,
                    timeliness = it.timeliness?.name,
                )
            }

        val allItems = when (selected) {
            LogbookFilter.HARVESTS -> harvestEntries
            LogbookFilter.SAMPLINGS -> samplingEntries
            LogbookFilter.THINNINGS -> thinningEntries
            LogbookFilter.ALL -> (harvestEntries + samplingEntries + thinningEntries)
        }

        return allItems
            .sortedByDescending { it.settledAt }
            .groupBy { entry ->
                val day = entry.settledAt.atZone(clock.zone).toLocalDate()
                when {
                    !day.isBefore(weekStart) -> LogbookPeriod.THIS_WEEK
                    YearMonth.from(day) == month -> LogbookPeriod.THIS_MONTH
                    else -> LogbookPeriod.EARLIER
                }
            }
            .map { (period, items) -> LogbookGroup(period = period, entries = items) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
