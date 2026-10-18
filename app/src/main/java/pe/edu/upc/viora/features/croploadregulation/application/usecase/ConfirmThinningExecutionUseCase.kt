package pe.edu.upc.viora.features.croploadregulation.application.usecase

import java.time.LocalDate
import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository

/** Records the thinning done in the field (P62); the answer is what P63 shows. */
class ConfirmThinningExecutionUseCase @Inject constructor(private val repository: ThinningPrescriptionRepository) {
    suspend operator fun invoke(
        plotId: String,
        campaignYear: Int,
        prescriptionId: String,
        executedOn: LocalDate,
        removalPercentage: Double,
        notes: String?,
    ): AppResult<ThinningExecution> =
        repository.confirmExecution(plotId, campaignYear, prescriptionId, executedOn, removalPercentage, notes)
}
