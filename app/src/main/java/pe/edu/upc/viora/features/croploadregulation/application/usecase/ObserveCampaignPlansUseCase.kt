package pe.edu.upc.viora.features.croploadregulation.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningPrescriptionRepository

/** The cached thinning plans of every plot for a campaign (P60). */
class ObserveCampaignPlansUseCase @Inject constructor(private val repository: ThinningPrescriptionRepository) {
    operator fun invoke(campaignYear: Int): Flow<List<CampaignPlan>> = repository.observeCampaign(campaignYear)
}
