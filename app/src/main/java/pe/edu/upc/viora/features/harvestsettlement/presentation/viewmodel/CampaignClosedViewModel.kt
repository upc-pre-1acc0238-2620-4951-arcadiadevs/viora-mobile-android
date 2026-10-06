package pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.DiscardPendingSettlementUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePendingSettlementsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CampaignClosedUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CommercialSizeGrade
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleHarvestRules
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettlementReceipt
import pe.edu.upc.viora.features.phenology.presentation.ui.tonnesPerHectare
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/**
 * Logic of P73: the receipt of a plot's campaign, from the server settlement (closed) or from the
 * settlement still waiting on the phone (pending, with its conflict and failed variants). It
 * follows both stores, so a pending screen turns into the closed one when the sync lands. The
 * plot and campaign come from the route arguments (`plotId`, `campaignYear`).
 *
 * Priority: a CONFLICT row, then a cached settlement, then a PENDING / FAILED row. When neither a
 * settlement nor a pending row exists the settlements of the plot are refreshed once; if that
 * does not find it either the state is [CampaignClosedUiState.Missing].
 */
@HiltViewModel
class CampaignClosedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlot: ObservePlotUseCase,
    observeSettled: ObserveSettledHarvestsUseCase,
    observePending: ObservePendingSettlementsUseCase,
    private val refreshSettled: RefreshSettledHarvestsUseCase,
    private val discardPending: DiscardPendingSettlementUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "CampaignClosedRoute needs a plotId" }
    private val campaignYear: Int = checkNotNull(savedStateHandle.get<Int>("campaignYear")) { "CampaignClosedRoute needs a campaignYear" }

    private val searching = MutableStateFlow(true)

    private val settlement = observeSettled()
    private val pending = observePending()

    val uiState: StateFlow<CampaignClosedUiState> = combine(
        observePlot(PlotId(plotId)),
        settlement,
        pending,
        searching,
    ) { plot, settled, queued, isSearching ->
        build(plot, settled.firstOrNull(::isThisCampaign), queued.firstOrNull(::isThisCampaign), isSearching)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CampaignClosedUiState.Loading)

    init {
        viewModelScope.launch {
            combine(settlement, pending) { settled, queued -> settled.any(::isThisCampaign) || queued.any(::isThisCampaign) }
                .distinctUntilChanged()
                .collectLatest { found ->
                    if (found) {
                        searching.value = false
                    } else {
                        searching.value = true
                        refreshSettled(listOf(plotId))
                        searching.value = false
                    }
                }
        }
    }

    /** "Entendido" on the 409 content: forgets the pending settlement; the screen then closes. */
    fun acknowledgeConflict(onDone: () -> Unit) {
        viewModelScope.launch {
            discardPending(plotId, campaignYear)
            onDone()
        }
    }

    /** "Ver comprobante" on the 409 content: forgets the pending row so the server's receipt shows. */
    fun showExistingReceipt() {
        viewModelScope.launch { discardPending(plotId, campaignYear) }
    }

    private fun build(plot: Plot?, settled: HarvestSettlement?, queued: PendingSettlement?, isSearching: Boolean): CampaignClosedUiState = when {
        queued?.status == PendingStatus.CONFLICT -> CampaignClosedUiState.Conflict(plot?.name.orEmpty(), campaignYear, queued.existing)
        settled != null -> CampaignClosedUiState.Closed(receiptOf(plot, settled))
        queued != null -> CampaignClosedUiState.Pending(receiptOf(plot, queued), failed = queued.status == PendingStatus.FAILED)
        isSearching -> CampaignClosedUiState.Loading
        else -> CampaignClosedUiState.Missing
    }

    /** The visible date is the weighing date; older backends do not send it, so the settle moment (device zone) stands in. */
    private fun receiptOf(plot: Plot?, settled: HarvestSettlement) = receipt(
        plot = plot,
        greenKg = settled.greenKg,
        blackKg = settled.blackKg,
        totalKg = settled.totalYieldKg,
        weighedOn = settled.weighedOn ?: settled.settledAt.atZone(clock.zone).toLocalDate(),
        receiptNumber = settled.receiptNumber?.takeIf { it.isNotBlank() },
        calibre = settled.commercialSizeGrade?.takeIf { it.isNotBlank() }
            ?: settled.commercialFruitsPerKg?.let(::calibreLabel),
    )

    private fun receiptOf(plot: Plot?, queued: PendingSettlement) = receipt(
        plot = plot,
        greenKg = queued.draft.greenOlivesKg,
        blackKg = queued.draft.blackOlivesKg,
        totalKg = queued.draft.greenOlivesKg + queued.draft.blackOlivesKg,
        weighedOn = queued.draft.weighedOn,
        receiptNumber = null,
        calibre = queued.draft.commercialFruitsPerKg?.let(::calibreLabel),
    )

    private fun receipt(
        plot: Plot?,
        greenKg: Double,
        blackKg: Double,
        totalKg: Double,
        weighedOn: LocalDate,
        receiptNumber: String?,
        calibre: String?,
    ) = SettlementReceipt(
        plotId = plotId,
        plotName = plot?.name.orEmpty(),
        variety = plot?.variety,
        campaignYear = campaignYear,
        greenKg = greenKg,
        blackKg = blackKg,
        totalKg = totalKg,
        greenShare = SettleHarvestRules.greenShare(greenKg, blackKg),
        tonnesPerHectare = if (plot != null && plot.areaHectares > 0.0 && totalKg > 0.0) tonnesPerHectare(totalKg, plot.areaHectares) else null,
        weighedOn = weighedOn,
        receiptNumber = receiptNumber,
        calibre = calibre,
    )

    /** "101/110" for a count inside the scale, else the plain count. */
    private fun calibreLabel(fruitsPerKg: Double): String =
        CommercialSizeGrade.forCount(fruitsPerKg)?.label ?: fruitsPerKg.toInt().toString()

    private fun isThisCampaign(settled: HarvestSettlement) = settled.plotId == plotId && settled.campaignYear == campaignYear

    private fun isThisCampaign(queued: PendingSettlement) = queued.draft.plotId == plotId && queued.draft.campaignYear == campaignYear

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
