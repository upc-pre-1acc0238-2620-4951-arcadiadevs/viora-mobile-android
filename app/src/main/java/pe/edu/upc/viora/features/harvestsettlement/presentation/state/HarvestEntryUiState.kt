package pe.edu.upc.viora.features.harvestsettlement.presentation.state

import pe.edu.upc.viora.features.harvestsettlement.domain.PlotToSettle
import pe.edu.upc.viora.features.plotmanagement.application.published.Plot

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

/** The active [plots] as candidates to settle; [pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePlotsToSettleUseCase] keeps those still to register. */
fun List<Plot>.toSettleCandidates(): List<PlotToSettle> =
    filter { it.isActive }.map { PlotToSettle(it.id.value, it.name, it.variety, it.areaHectares) }
