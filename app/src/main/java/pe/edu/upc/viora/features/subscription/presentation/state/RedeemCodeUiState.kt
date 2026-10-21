package pe.edu.upc.viora.features.subscription.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode

/** P05 · Canjear código. The field starts with the prefix every code has, as in the design. */
data class RedeemCodeUiState(
    val code: String = InvitationCode.PREFIX,
    val isRedeeming: Boolean = false,
    val failure: RedeemFailure? = null,
    /** Set once the backend accepted the code; the screen then opens P06. */
    val redeemed: CooperativeMembership? = null,
) {
    val canRedeem: Boolean get() = InvitationCode.isWellFormed(code) && !isRedeeming

    /** P05 "Este código no funcionó": its own title, voice and button. */
    val isCodeRejected: Boolean get() = failure == RedeemFailure.NotRedeemable
}

sealed interface RedeemFailure {
    data object NotRedeemable : RedeemFailure
    data object AlreadySubscribed : RedeemFailure
    data object TooManyAttempts : RedeemFailure
    data class Other(val error: AppError) : RedeemFailure
}
