package pe.edu.upc.viora.features.harvestsettlement.presentation.state

import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

/** A plot whose harvest is not registered yet for the campaign ("Por registrar", P70). */
data class PlotToSettle(
    val plotId: String,
    val name: String,
    val variety: OliveVariety,
    val hectares: Double,
)

/** What the Home card and the plot picker (P70) need: the plots left to settle in [campaignYear]. */
data class HarvestEntryUiState(
    val campaignYear: Int,
    val plots: List<PlotToSettle>,
    val totalPlots: Int,
) {
    val pendingCount: Int get() = plots.size

    /** Plots already registered (or waiting to sync), for the card's progress capsule. */
    val registeredCount: Int get() = totalPlots - plots.size

    /** The Home card only shows while there is something to register. */
    val showCard: Boolean get() = plots.isNotEmpty()
}

/** Pure rule behind "Por registrar". */
object HarvestEntryRules {

    /**
     * Active plots without a settlement for [year] and without a local settlement waiting to sync
     * (PENDING) or to be corrected (FAILED). A CONFLICT row does not hide the plot: the server
     * already has a settlement, and opening the form shows the "already settled" dialog.
     */
    fun plotsToSettle(
        plots: List<Plot>,
        settlements: List<HarvestSettlement>,
        pending: List<PendingSettlement>,
        year: Int,
    ): List<PlotToSettle> {
        val settled = settlements.filter { it.campaignYear == year }.map { it.plotId }.toSet()
        val queued = pending
            .filter { it.draft.campaignYear == year && it.status != PendingStatus.CONFLICT }
            .map { it.draft.plotId }
            .toSet()
        return plots
            .filter { it.isActive && it.id.value !in settled && it.id.value !in queued }
            .map { PlotToSettle(it.id.value, it.name, it.variety, it.areaHectares) }
    }
}
