package pe.edu.upc.viora.features.home.domain

import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

/**
 * Which plot the Home talks about when the producer has several: the one they chose last, and
 * while they have not chosen (or that plot is gone, e.g. archived) the biggest one, with the
 * name as the tie-break so the answer never changes between runs.
 */
object FocusedPlotRule {

    fun pick(plots: List<Plot>, chosenId: String?): Plot? =
        plots.firstOrNull { it.id.value == chosenId }
            ?: plots.sortedWith(compareByDescending<Plot> { it.areaHectares }.thenBy { it.name }).firstOrNull()
}
