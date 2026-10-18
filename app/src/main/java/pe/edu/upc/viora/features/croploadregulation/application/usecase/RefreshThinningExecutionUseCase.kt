package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository

/** Downloads the recorded thinning of a prescription again (P63 opened from P61). */
class RefreshThinningExecutionUseCase @Inject constructor(private val repository: ThinningPrescriptionRepository) {
    suspend operator fun invoke(plotId: String, campaignYear: Int, prescriptionId: String): AppResult<Unit> =
        repository.refreshExecution(plotId, campaignYear, prescriptionId)
}
