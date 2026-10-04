package pe.edu.upc.viora.features.phenology.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.domain.repository.HarvestRecordRepository

class RemoveHarvestRecordUseCase @Inject constructor(
    private val repository: HarvestRecordRepository,
) {
    suspend operator fun invoke(plotId: String, recordId: String): AppResult<Unit> =
        repository.remove(plotId, recordId)
}
