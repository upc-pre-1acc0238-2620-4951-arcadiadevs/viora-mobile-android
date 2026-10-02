package pe.edu.upc.viora.features.plotmanagement.domain.entity

import java.time.LocalDate
import kotlin.math.roundToInt
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/**
 * An olive orchard plot ("lote" in the UI). [areaHectares] and [treesPerHectare] are computed
 * by the backend from the polygon and the planting frame.
 *
 * [outline] is the polygon ring without the repeated closing vertex. [revision] is the
 * optimistic-locking version that must be echoed back (If-Match) when updating the plot.
 */
data class Plot(
    val id: PlotId,
    val name: String,
    val variety: OliveVariety,
    val areaHectares: Double,
    val treesPerHectare: Int,
    val rowSpacingMeters: Double,
    val treeSpacingMeters: Double,
    val outline: List<GeoPoint>,
    val lastPruningDate: LocalDate?,
    val isActive: Boolean,
    val revision: Long,
) {
    /** Approximate tree count of the whole plot, as shown on the plot card. */
    val estimatedTrees: Int
        get() = (areaHectares * treesPerHectare).roundToInt()
}
