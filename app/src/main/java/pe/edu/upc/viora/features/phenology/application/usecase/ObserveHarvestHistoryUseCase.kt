package pe.edu.upc.viora.features.phenology.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.repository.HarvestRecordRepository

class ObserveHarvestHistoryUseCase @Inject constructor(
    private val repository: HarvestRecordRepository,
) {
    operator fun invoke(plotId: String): Flow<List<HarvestRecord>> = repository.observeRecords(plotId)
}
