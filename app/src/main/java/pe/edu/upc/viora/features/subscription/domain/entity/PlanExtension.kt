package pe.edu.upc.viora.features.subscription.domain.entity

import java.math.BigDecimal
import java.time.Instant

/** How the plan in force can grow now, as the backend decides it (GET extension-terms). */
enum class ExtensionAction {
    /** An individual plan: pay the new hectares for the months left (P102). */
    PAY,

    /** A plan activated with a code: redeem an extension code (P103). */
    CODE,

    /** The last 30 days of an individual plan: renew with more hectares instead (P02 renewal). */
    RENEW,

    /** No plan in force: buy one (P02). */
    NONE,
}

/**
 * The terms of a paid extension. [monthlyRatePerHa] already includes the tax, so the total is
 * hectares × rate × [billableMonths], the same product the backend charges.
 */
data class ExtensionPricing(
    val billableMonths: Int,
    val monthlyRatePerHa: BigDecimal,
    val currency: String,
    val minimum: BigDecimal,
    val increment: BigDecimal,
) {
    fun total(addedHectares: BigDecimal): BigDecimal =
        addedHectares.multiply(monthlyRatePerHa).multiply(BigDecimal(billableMonths))
}

data class PlanExtensionTerms(
    val subscriptionId: String,
    val action: ExtensionAction,
    val quotaHectares: BigDecimal,
    val endsAt: Instant?,
    /** Set only for [ExtensionAction.PAY]. */
    val pricing: ExtensionPricing?,
)

enum class ExtensionStatus { PENDING_PAYMENT, APPLIED }

/** Hectares added to a plan without changing when it ends (backend QuotaExtensionResource). */
data class QuotaExtension(
    val id: String,
    val subscriptionId: String,
    val status: ExtensionStatus,
    val addedHectares: BigDecimal,
    /** The plan's quota now; it includes [addedHectares] once [status] is APPLIED. */
    val quotaHectares: BigDecimal,
    val endsAt: Instant?,
    val billedMonths: Int?,
    val currency: String?,
    val total: BigDecimal?,
    /** The latest Mercado Pago attempt (OPEN, PENDING, APPROVED, REJECTED…), null before a checkout. */
    val paymentStatus: String?,
) {
    val isApplied: Boolean get() = status == ExtensionStatus.APPLIED
    val isRejected: Boolean get() = paymentStatus == "REJECTED"
}
