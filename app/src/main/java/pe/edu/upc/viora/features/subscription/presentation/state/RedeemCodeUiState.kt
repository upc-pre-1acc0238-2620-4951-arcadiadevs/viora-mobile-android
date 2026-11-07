package pe.edu.upc.viora.features.subscription.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreview
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode

/**
 * P05 · Canjear código and P05b · Aceptar y compartir. The field starts with the prefix every code has, as in the
 * design. The code is checked first ([isValidating]); once the backend says what it offers ([preview]) the producer
 * reads what the manager will see and accepts, and only then is it redeemed ([isAccepting]).
 */
data class RedeemCodeUiState(
    val code: String = InvitationCode.PREFIX,
    val isValidating: Boolean = false,
    /** Set while P05b is open: what the code offers and the consent text to accept. */
    val preview: CodePreview? = null,
    val isAccepting: Boolean = false,
    val failure: RedeemFailure? = null,
    /** Set once the backend redeemed the code; the screen then opens P06. */
    val redeemed: CooperativeMembership? = null,
) {
    val canValidate: Boolean get() = InvitationCode.isWellFormed(code) && !isValidating

    /** P05 "Este código no funcionó": its own title, voice and button. */
    val isCodeRejected: Boolean get() = preview == null && failure == RedeemFailure.NotRedeemable

    /** P05 · Ya tienes un plan: plans do not combine, so it only offers to go back to Mi plan. */
    val hasActivePlan: Boolean get() = preview == null && failure == RedeemFailure.AlreadySubscribed
}

sealed interface RedeemFailure {
    data object NotRedeemable : RedeemFailure
    data object AlreadySubscribed : RedeemFailure
    data object TooManyAttempts : RedeemFailure
    data class Other(val error: AppError) : RedeemFailure
}
