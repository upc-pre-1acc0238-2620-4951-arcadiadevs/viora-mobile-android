package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import java.io.File
import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.AgronomicDossierRepository

class DownloadDossierPdfUseCase @Inject constructor(
    private val repository: AgronomicDossierRepository,
) {
    suspend operator fun invoke(plotId: String, campaignYear: Int): AppResult<File> = repository.downloadPdf(plotId, campaignYear)
}
