package pe.edu.upc.viora.features.phenology.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.repository.HarvestRecordRepository

class RectifyHarvestYieldUseCase @Inject constructor(
    private val repository: HarvestRecordRepository,
) {
    suspend operator fun invoke(plotId: String, recordId: String, totalYieldKg: Double): AppResult<HarvestRecord> =
        repository.rectify(plotId, recordId, totalYieldKg)
}
