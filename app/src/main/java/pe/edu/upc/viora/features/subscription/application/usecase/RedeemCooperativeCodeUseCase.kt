package pe.edu.upc.viora.features.subscription.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.repository.CooperativeCodeRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode

class RedeemCooperativeCodeUseCase @Inject constructor(
    private val repository: CooperativeCodeRepository,
) {
    suspend operator fun invoke(code: InvitationCode): RedemptionOutcome = repository.redeem(code)
}
