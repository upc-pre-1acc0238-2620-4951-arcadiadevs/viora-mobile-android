package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.AgronomicDossierRepository

/** The newest campaign of a plot with a sealed dossier; null when none was sealed. */
class FindLatestDossierCampaignUseCase @Inject constructor(
    private val repository: AgronomicDossierRepository,
) {
    suspend operator fun invoke(plotId: String): AppResult<Int?> = repository.latestSealedCampaign(plotId)
}
