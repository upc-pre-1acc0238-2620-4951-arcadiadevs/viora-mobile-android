package pe.edu.upc.viora.features.phenology.domain.repository

import java.time.MonthDay
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.phenology.domain.OlivePhase
import pe.edu.upc.viora.features.phenology.domain.PhaseAdjustments

/** Remembers, for each plot, the phase start dates its producer corrected. */
interface PhaseAdjustmentsRepository {

    /** The corrections of the plot; empty while the producer made none. */
    fun observe(plotId: String): Flow<PhaseAdjustments>

    suspend fun adjust(plotId: String, phase: OlivePhase, on: MonthDay)

    /** The start of [phase] goes back to the usual date. */
    suspend fun reset(plotId: String, phase: OlivePhase)
}
