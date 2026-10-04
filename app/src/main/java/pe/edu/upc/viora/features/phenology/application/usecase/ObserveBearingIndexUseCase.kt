package pe.edu.upc.viora.features.phenology.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.domain.repository.HarvestRecordRepository

class ObserveBearingIndexUseCase @Inject constructor(
    private val repository: HarvestRecordRepository,
) {
    operator fun invoke(plotId: String): Flow<BearingIndex?> = repository.observeBearingIndex(plotId)
}
