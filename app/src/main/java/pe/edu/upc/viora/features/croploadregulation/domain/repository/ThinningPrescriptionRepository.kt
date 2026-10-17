package pe.edu.upc.viora.features.croploadregulation.domain.repository

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CachedPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription

/**
 * The thinning plan of a plot (P61), the full bloom that opens its window (P64) and the thinning recorded against it
 * (P62/P63), cached for offline reading.
 */
interface ThinningPrescriptionRepository {

    /**
     * The cached plan of [plotId] for [campaignYear]: null until it was downloaded once; a
     * [CachedPrescription] without prescription when the plot has none for that campaign (nothing sampled yet).
     */
    fun observePrescription(plotId: String, campaignYear: Int): Flow<CachedPrescription?>

    /** Downloads the plan again; the backend re-evaluates it first, so a new profile or bloom shows up. */
    suspend fun refreshPrescription(plotId: String, campaignYear: Int): AppResult<Unit>

    /** Records (or corrects) the full bloom of the campaign; the answer is the plan with its window. */
    suspend fun recordFullBloom(plotId: String, campaignYear: Int, observedOn: LocalDate): AppResult<ThinningPrescription>

    /** The cached thinning of the plan of [plotId] for [campaignYear]; null until it is recorded or downloaded. */
    fun observeExecution(plotId: String, campaignYear: Int): Flow<ThinningExecution?>

    /**
     * Records the thinning done in the field. The answer brings the load it left, the caliber and the yield (P63); the
     * plan is downloaded again so P61 shows it as done.
     */
    suspend fun confirmExecution(
        plotId: String,
        campaignYear: Int,
        prescriptionId: String,
        executedOn: LocalDate,
        removalPercentage: Double,
        notes: String?,
    ): AppResult<ThinningExecution>

    /** Downloads the recorded thinning again; succeeds without change when it was not recorded yet. */
    suspend fun refreshExecution(plotId: String, campaignYear: Int, prescriptionId: String): AppResult<Unit>
}
