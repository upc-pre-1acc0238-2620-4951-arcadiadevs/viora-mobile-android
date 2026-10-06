package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository

class SettleHarvestUseCase @Inject constructor(
    private val repository: HarvestSettlementRepository,
) {
    suspend operator fun invoke(draft: SettleHarvestDraft, idempotencyKey: String): AppResult<SettleOutcome> =
        repository.settle(draft, idempotencyKey)
}
