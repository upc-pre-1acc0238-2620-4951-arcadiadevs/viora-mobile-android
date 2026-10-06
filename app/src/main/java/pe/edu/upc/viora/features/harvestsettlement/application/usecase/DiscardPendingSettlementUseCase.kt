package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository

class DiscardPendingSettlementUseCase @Inject constructor(
    private val repository: HarvestSettlementRepository,
) {
    suspend operator fun invoke(plotId: String, campaignYear: Int): AppResult<Unit> =
        repository.discardPending(plotId, campaignYear)
}
