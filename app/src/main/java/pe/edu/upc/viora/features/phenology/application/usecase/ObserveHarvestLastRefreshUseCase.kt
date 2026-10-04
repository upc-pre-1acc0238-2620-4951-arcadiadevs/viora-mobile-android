package pe.edu.upc.viora.features.phenology.application.usecase

import javax.inject.Inject
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.phenology.domain.repository.HarvestRecordRepository

class ObserveHarvestLastRefreshUseCase @Inject constructor(
    private val repository: HarvestRecordRepository,
) {
    operator fun invoke(plotId: String): Flow<Instant?> = repository.observeLastRefresh(plotId)
}
