package pe.edu.upc.viora.features.harvestsettlement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository

class UpdatePendingSettlementUseCase @Inject constructor(
    private val repository: HarvestSettlementRepository,
) {
    suspend operator fun invoke(draft: SettleHarvestDraft): AppResult<Unit> = repository.updatePending(draft)
}
