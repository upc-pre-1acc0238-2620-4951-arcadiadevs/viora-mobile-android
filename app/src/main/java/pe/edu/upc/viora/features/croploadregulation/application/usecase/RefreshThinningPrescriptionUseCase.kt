package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository

class RefreshThinningPrescriptionUseCase @Inject constructor(private val repository: ThinningPrescriptionRepository) {
    suspend operator fun invoke(plotId: String, campaignYear: Int): AppResult<Unit> =
        repository.refreshPrescription(plotId, campaignYear)
}
