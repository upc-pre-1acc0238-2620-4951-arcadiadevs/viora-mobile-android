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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookFilter
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookGroup
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookPeriod
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.LogbookUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettledHarvestEntry
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

/**
 * Logbook (P50), US29 slice: the producer's settled campaigns across all plots. It reads the
 * cache and refreshes every plot on entry; plot names come from the plot cache.
 */
@HiltViewModel
class LogbookViewModel @Inject constructor(
    observeSettlements: ObserveSettledHarvestsUseCase,
    private val observePlots: ObservePlotsUseCase,
    observePlotsLastRefresh: ObservePlotsLastRefreshUseCase,
    private val refreshPlots: RefreshPlotsUseCase,
    private val refreshSettlements: RefreshSettledHarvestsUseCase,
    private val clock: Clock,
) : ViewModel() {

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private val filter = MutableStateFlow(LogbookFilter.ALL)

    val uiState: StateFlow<LogbookUiState> = combine(
        observeSettlements(),
        observePlots(),
        observePlotsLastRefresh(),
        refreshState,
        filter,
    ) { settlements, plots, lastPlotRefresh, refresh, selected ->
        if (settlements.isEmpty() && !refresh.hasFinishedOnce) {
            LogbookUiState.Loading
        } else {
            LogbookUiState.Content(
                filter = selected,
                groups = groupsFor(selected, settlements, plots),
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

    /** The plot ids come from the cache; with nothing cached yet the plots are downloaded first. */
    private suspend fun refreshAll(): AppResult<Unit> {
        var plots = observePlots().first()
        if (plots.isEmpty()) {
            val plotsResult = refreshPlots()
            if (plotsResult is AppResult.Failure) return plotsResult
            plots = observePlots().first()
        }
        return refreshSettlements(plots.map { it.id.value })
    }

    private fun groupsFor(selected: LogbookFilter, settlements: List<HarvestSettlement>, plots: List<Plot>): List<LogbookGroup> {
        // Sampling and thinning records do not exist yet: those filters are empty by design.
        if (selected == LogbookFilter.SAMPLINGS || selected == LogbookFilter.THINNINGS) return emptyList()
        val names = plots.associate { it.id.value to it.name }
        val today = LocalDate.now(clock)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val month = YearMonth.from(today)
        return settlements
            .sortedByDescending { it.settledAt }
            .groupBy { settlement ->
                val day = settlement.settledAt.atZone(clock.zone).toLocalDate()
                when {
                    !day.isBefore(weekStart) -> LogbookPeriod.THIS_WEEK
                    YearMonth.from(day) == month -> LogbookPeriod.THIS_MONTH
                    else -> LogbookPeriod.EARLIER
                }
            }
            .map { (period, items) ->
                LogbookGroup(
                    period = period,
                    entries = items.map {
                        SettledHarvestEntry(
                            id = it.id,
                            plotId = it.plotId,
                            plotName = names[it.plotId],
                            campaignYear = it.campaignYear,
                            totalYieldKg = it.totalYieldKg,
                            status = it.status,
                            settledAt = it.settledAt,
                        )
                    },
                )
            }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
