package pe.edu.upc.viora.features.subscription.presentation.state

import java.math.BigDecimal
import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.subscription.domain.entity.ExtensionAction
import pe.edu.upc.viora.features.subscription.domain.entity.ExtensionPricing
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualCheckout
import pe.edu.upc.viora.features.subscription.domain.entity.QuotaExtension

/** P102 · Ampliar mi plan and the P04 results of its payment (Figma section 22). */
sealed interface PlanExtensionUiState {
    data object Loading : PlanExtensionUiState

    data class Failed(val error: AppError) : PlanExtensionUiState

    /** The plan does not grow by payment: the screen hands over to P103, the renewal or P02. */
    data class Redirect(val action: ExtensionAction) : PlanExtensionUiState

    /** P102: the producer picks the hectares to add and sees what they pay today. */
    data class Selecting(
        val subscriptionId: String,
        val quotaHectares: BigDecimal,
        val endsAt: Instant?,
        val pricing: ExtensionPricing,
        val addedHectares: BigDecimal,
        val isWorking: Boolean = false,
        val error: AppError? = null,
        /** Set once the checkout link is ready; the screen opens it in the browser. */
        val checkout: IndividualCheckout? = null,
    ) : PlanExtensionUiState {
        val newQuota: BigDecimal get() = quotaHectares.add(addedHectares)
        val total: BigDecimal get() = pricing.total(addedHectares)
        val canDecrease: Boolean get() = addedHectares > pricing.minimum
    }

    /** P04 · Confirmando tu pago: waiting for Mercado Pago's notification to reach the backend. */
    data object Verifying : PlanExtensionUiState

    /** P04 · Ampliación aprobada. */
    data class Applied(val extension: QuotaExtension) : PlanExtensionUiState {
        val previousQuota: BigDecimal get() = extension.quotaHectares.subtract(extension.addedHectares)
    }

    /** P04 · Pago rechazado: nothing was added; the producer can try again. */
    data object Rejected : PlanExtensionUiState

    /** The payment was not confirmed while the screen waited; the producer can check again. */
    data object Unconfirmed : PlanExtensionUiState
}
