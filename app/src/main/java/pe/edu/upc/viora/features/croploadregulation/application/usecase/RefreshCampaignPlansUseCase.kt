package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository

/** Downloads the thinning plans of every plot for a campaign (P60). */
class RefreshCampaignPlansUseCase @Inject constructor(private val repository: ThinningPrescriptionRepository) {
    suspend operator fun invoke(campaignYear: Int): AppResult<Unit> = repository.refreshCampaign(campaignYear)
}
