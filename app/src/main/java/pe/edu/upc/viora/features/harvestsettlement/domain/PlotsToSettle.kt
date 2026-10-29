package pe.edu.upc.viora.features.harvestsettlement.domain

import pe.edu.upc.viora.core.domain.OliveVariety
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus

/** An active plot whose harvest may still need registering ("Por registrar", P70). */
data class PlotToSettle(
    val plotId: String,
    val name: String,
    val variety: OliveVariety,
    val hectares: Double,
)

/** The rule behind "Por registrar". */
object PlotsToSettleRule {

    /**
     * The [candidates] (the active plots) without a settlement for [year] and without a local settlement
     * waiting to sync (PENDING) or to be corrected (FAILED). A CONFLICT row does not hide the plot: the
     * server already has a settlement, and opening the form shows the "already settled" dialog.
     */
    fun remaining(
        candidates: List<PlotToSettle>,
        settlements: List<HarvestSettlement>,
        pending: List<PendingSettlement>,
        year: Int,
    ): List<PlotToSettle> {
        val settled = settlements.filter { it.campaignYear == year }.map { it.plotId }.toSet()
        val queued = pending
            .filter { it.draft.campaignYear == year && it.status != PendingStatus.CONFLICT }
            .map { it.draft.plotId }
            .toSet()
        return candidates.filter { it.plotId !in settled && it.plotId !in queued }
    }
}
