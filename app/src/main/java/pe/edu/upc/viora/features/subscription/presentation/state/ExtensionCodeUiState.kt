package pe.edu.upc.viora.features.subscription.presentation.state

import pe.edu.upc.viora.features.subscription.domain.entity.QuotaExtension
import pe.edu.upc.viora.features.subscription.domain.valueobject.ExtensionCode

/** P103 · Ampliar con código. The field starts with the prefix every extension code has. */
data class ExtensionCodeUiState(
    val code: String = ExtensionCode.PREFIX,
    val isRedeeming: Boolean = false,
    val failure: RedeemFailure? = null,
    /** Set once the backend added the hectares; the screen then opens P104. */
    val applied: QuotaExtension? = null,
) {
    val canRedeem: Boolean get() = ExtensionCode.isWellFormed(code) && !isRedeeming

    /** P103 "Este código no suma": its own title, voice and button. */
    val isCodeRejected: Boolean get() = failure == RedeemFailure.NotRedeemable
}
