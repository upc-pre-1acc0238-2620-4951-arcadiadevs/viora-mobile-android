package pe.edu.upc.viora.features.plotmanagement.domain.entity

/**
 * A sector of the list of the license that sponsors the member's plan (P24 · Elegir sector). A sector is a label of
 * the plot for the manager, never a boundary on the map.
 */
data class SectorZone(val id: String, val name: String)

/** What the member answers when asked where a plot is. */
sealed interface SectorChoice {
    /** A sector of the license's list. */
    data class Listed(val sector: SectorZone) : SectorChoice

    /** «Mi sector no está»: a name the manager reviews; the plot is «Por confirmar» until then. */
    data class Written(val name: String) : SectorChoice

    companion object {
        /** The length the server accepts for a written sector. */
        const val MIN_NAME_LENGTH = 2
        const val MAX_NAME_LENGTH = 60
    }
}

/** Where a plot stands in the list of sectors. */
enum class PlotSectorStatus {
    ASSIGNED,

    /** «Por confirmar»: the member wrote a sector the manager has not reviewed yet. */
    PENDING,
}

/** The sector of one of the member's plots, as the license keeps it. */
data class PlotSector(
    val plotId: String,
    val status: PlotSectorStatus,
    val sectorName: String?,
    val proposedName: String?,
) {
    /** What the member reads: the sector, or the name they wrote while it waits. */
    val label: String? get() = if (status == PlotSectorStatus.ASSIGNED) sectorName else proposedName ?: sectorName
}
