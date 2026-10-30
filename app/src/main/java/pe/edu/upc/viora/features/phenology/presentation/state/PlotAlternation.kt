package pe.edu.upc.viora.features.phenology.presentation.state

import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord

/** What the "Tu alternancia" card of the Home says about the plot in focus (phenology draws it for the Home). */
sealed interface PlotAlternation {

    /** Fewer than three campaigns: there is no index yet, [missing] more are needed. */
    data class Insufficient(val missing: Int) : PlotAlternation

    /** The last campaigns ([records], oldest first) with what Viora says about them. */
    data class Ready(
        val records: List<HarvestRecord>,
        val areaHectares: Double,
        val voice: Voice,
    ) : PlotAlternation
}
