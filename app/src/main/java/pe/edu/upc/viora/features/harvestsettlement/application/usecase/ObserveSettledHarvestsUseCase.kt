package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository

class ObserveSettledHarvestsUseCase @Inject constructor(
    private val repository: HarvestSettlementRepository,
) {
    operator fun invoke(): Flow<List<HarvestSettlement>> = repository.observeAll()
}
