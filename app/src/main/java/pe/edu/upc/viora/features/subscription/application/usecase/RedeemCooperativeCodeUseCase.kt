package pe.edu.upc.viora.features.subscription.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreview
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreviewOutcome
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.repository.CooperativeCodeRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode

/** P05 → P05b: what the code offers, before the producer accepts anything. */
class PreviewCooperativeCodeUseCase @Inject constructor(
    private val repository: CooperativeCodeRepository,
) {
    suspend operator fun invoke(code: InvitationCode): CodePreviewOutcome = repository.preview(code)
}

/**
 * P05b "Aceptar y activar": redeems the code under the consent text the producer read. The membership names the
 * manager of the preview, since the redemption answers only the plan.
 */
class RedeemCooperativeCodeUseCase @Inject constructor(
    private val repository: CooperativeCodeRepository,
) {
    suspend operator fun invoke(code: InvitationCode, accepted: CodePreview): RedemptionOutcome =
        when (val outcome = repository.redeem(code, accepted.consentTextVersion)) {
            is RedemptionOutcome.Redeemed -> RedemptionOutcome.Redeemed(
                outcome.membership.copy(
                    endsAt = outcome.membership.endsAt ?: accepted.planEndsAt,
                    managerName = accepted.managerName,
                ),
            )
            else -> outcome
        }
}
