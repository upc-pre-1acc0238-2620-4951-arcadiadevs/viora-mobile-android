package pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Year
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePendingSettlementsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.HarvestEntryRules
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.HarvestEntryUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase

/**
 * Home entry to the settle flow: which plots are still "por registrar" in the current campaign.
 * It feeds the Home "AHORA · COSECHA" card and the plot picker (P70). The settlements cache is
 * refreshed once the plots are known, so a plot settled from another device does not show up.
 */
@HiltViewModel
class HarvestEntryViewModel @Inject constructor(
    private val observePlots: ObservePlotsUseCase,
    observeSettlements: ObserveSettledHarvestsUseCase,
    observePending: ObservePendingSettlementsUseCase,
    private val refreshSettlements: RefreshSettledHarvestsUseCase,
    private val clock: Clock,
) : ViewModel() {

    /** False until the first refresh finished (even a failed one), so the card does not flash. */
    private val ready = MutableStateFlow(false)

    val uiState: StateFlow<HarvestEntryUiState> = combine(
        observePlots(),
        observeSettlements(),
        observePending(),
        ready,
    ) { plots, settlements, pending, isReady ->
        val year = currentYear()
        HarvestEntryUiState(
            campaignYear = year,
            plots = if (isReady) HarvestEntryRules.plotsToSettle(plots, settlements, pending, year) else emptyList(),
            totalPlots = plots.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HarvestEntryUiState(currentYear(), emptyList(), 0))

    init {
        viewModelScope.launch {
            observePlots().map { plots -> plots.map { it.id.value } }.distinctUntilChanged().collect { ids ->
                if (ids.isNotEmpty()) {
                    refreshSettlements(ids)
                    ready.value = true
                }
            }
        }
    }

    private fun currentYear(): Int = Year.now(clock).value

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
