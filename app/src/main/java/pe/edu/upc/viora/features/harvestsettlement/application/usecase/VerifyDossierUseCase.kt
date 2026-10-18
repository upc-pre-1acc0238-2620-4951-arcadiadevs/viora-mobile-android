package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierVerification
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.AgronomicDossierRepository

class VerifyDossierUseCase @Inject constructor(
    private val repository: AgronomicDossierRepository,
) {
    suspend operator fun invoke(verificationHash: String): AppResult<DossierVerification> = repository.verify(verificationHash)
}
