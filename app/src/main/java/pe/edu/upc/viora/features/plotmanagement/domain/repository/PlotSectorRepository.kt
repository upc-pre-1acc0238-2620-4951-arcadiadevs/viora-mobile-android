package pe.edu.upc.viora.features.plotmanagement.domain.repository

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotSector
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorChoice
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorZone

/**
 * The sectors of a member's plots. They are kept by the license, never in the plot, so a producer without a
 * sponsored plan has none and every call answers [pe.edu.upc.viora.core.domain.AppError.Forbidden].
 */
interface PlotSectorRepository {

    /** The license's list of sectors, in the server's order. */
    suspend fun sectors(): AppResult<List<SectorZone>>

    /** The sectors of the member's plots; a plot that is missing has none yet. */
    suspend fun plotSectors(): AppResult<List<PlotSector>>

    /** Gives [plotId] the sector the member chose or wrote. */
    suspend fun assign(plotId: String, choice: SectorChoice): AppResult<PlotSector>
}
