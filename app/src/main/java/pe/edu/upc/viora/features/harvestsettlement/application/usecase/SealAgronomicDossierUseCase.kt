package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.AgronomicDossierRepository

/** Seals the dossier of a settled campaign, or downloads it again when it already was (the backend is idempotent). */
class SealAgronomicDossierUseCase @Inject constructor(
    private val repository: AgronomicDossierRepository,
) {
    suspend operator fun invoke(plotId: String, campaignYear: Int): AppResult<Unit> = repository.seal(plotId, campaignYear)
}
