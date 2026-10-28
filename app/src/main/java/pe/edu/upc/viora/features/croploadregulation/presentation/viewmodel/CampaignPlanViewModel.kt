package pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel

import pe.edu.upc.viora.core.domain.isConnectivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveCampaignPlansUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RefreshCampaignPlansUseCase
import pe.edu.upc.viora.features.croploadregulation.presentation.state.CampaignPlanRules
import pe.edu.upc.viora.features.croploadregulation.presentation.state.CampaignPlanUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsLastRefreshUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RefreshPlotsUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveIncidentsUseCase
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus

/** P60 · Plan, the Plan tab: every plot with the state of its thinning plan, read from the phone and refreshed. */
@HiltViewModel
class CampaignPlanViewModel @Inject constructor(
    observePlots: ObservePlotsUseCase,
    observeLastRefresh: ObservePlotsLastRefreshUseCase,
    observeIncidents: ObserveIncidentsUseCase,
    private val observeCampaignPlans: ObserveCampaignPlansUseCase,
    private val refreshCampaignPlans: RefreshCampaignPlansUseCase,
    private val refreshPlots: RefreshPlotsUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now(clock)

    private val campaignYear = MutableStateFlow(today.year)

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val plans = campaignYear.flatMapLatest { year -> observeCampaignPlans(year).map { year to it } }

    private val hasUnreadAlerts: Flow<Boolean> =
        observeIncidents(null).map { incidents -> incidents.any { it.status != IncidentStatus.NORMALIZED } }

    val uiState: StateFlow<CampaignPlanUiState> = combine(
        observePlots(),
        plans,
        observeLastRefresh(),
        hasUnreadAlerts,
        refreshState,
    ) { plots, (year, campaign), lastRefresh, unread, refresh ->
        CampaignPlanUiState(
            campaignYear = year,
            campaignYears = CampaignPlanRules.campaignYears(today),
            today = today,
            rows = CampaignPlanRules.rows(plots, campaign, today),
            hasPlots = plots.isNotEmpty(),
            // Nothing on the phone yet: wait for the first answer before saying there are no plots.
            isLoading = plots.isEmpty() && !refresh.hasFinishedOnce,
            isRefreshing = refresh.isRefreshing,
            isOffline = refresh.error.isConnectivity,
            error = refresh.error,
            lastRefresh = lastRefresh,
            hasUnreadAlerts = unread,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CampaignPlanUiState(
            campaignYear = today.year,
            campaignYears = CampaignPlanRules.campaignYears(today),
            today = today,
        ),
    )

    init {
        refresh()
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val plotsResult = refreshPlots()
            val plansResult = refreshCampaignPlans(campaignYear.value)
            val error = (plotsResult as? AppResult.Failure)?.error ?: (plansResult as? AppResult.Failure)?.error
            refreshState.update { RefreshState(isRefreshing = false, hasFinishedOnce = true, error = error) }
        }
    }

    /** The chip "Campaña 2026": another campaign shows its cached plans and downloads them again. */
    fun selectCampaign(year: Int) {
        if (year == campaignYear.value) return
        campaignYear.value = year
        viewModelScope.launch {
            val result = refreshCampaignPlans(year)
            refreshState.update { it.copy(error = (result as? AppResult.Failure)?.error) }
        }
    }
}
