package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository

class RefreshSettledHarvestsUseCase @Inject constructor(
    private val repository: HarvestSettlementRepository,
) {
    suspend operator fun invoke(plotIds: List<String>): AppResult<Unit> = repository.refreshAll(plotIds)
}
