package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.CachedDossier
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.AgronomicDossierRepository

class ObserveAgronomicDossierUseCase @Inject constructor(
    private val repository: AgronomicDossierRepository,
) {
    operator fun invoke(plotId: String, campaignYear: Int): Flow<CachedDossier?> =
        repository.observeDossier(plotId, campaignYear)
}
