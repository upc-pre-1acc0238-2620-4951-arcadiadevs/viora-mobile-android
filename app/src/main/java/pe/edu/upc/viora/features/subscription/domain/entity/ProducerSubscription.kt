package pe.edu.upc.viora.features.subscription.domain.entity

import java.math.BigDecimal
import java.time.Instant
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus
import pe.edu.upc.viora.core.domain.Uuid

/**
 * Server-verified subscription record.
 * Application access is strictly governed by [entitlementActive] as confirmed by the backend.
 */
data class ProducerSubscription(
    val id: String,
    val mode: SubscriptionMode,
    val planCode: ProducerPlanCode?,
    val status: SubscriptionStatus,
    val capacity: PlanCapacity,
    val currency: String?,
    val baseAmount: BigDecimal?,
    val taxAmount: BigDecimal?,
    val totalAmount: BigDecimal?,
    val startsAt: Instant?,
    val endsAt: Instant?,
    val entitlementActive: Boolean,
    val cooperativeId: String?,
    val paymentStatus: pe.edu.upc.viora.features.subscription.domain.valueobject.IndividualPaymentStatus =
        pe.edu.upc.viora.features.subscription.domain.valueobject.IndividualPaymentStatus.UNKNOWN,
) {
    init {
        Uuid.parse(id)
        if (totalAmount != null && baseAmount != null && taxAmount != null) {
            require(baseAmount.signum() >= 0 && taxAmount.signum() >= 0 && totalAmount.signum() > 0)
            require(baseAmount.add(taxAmount).compareTo(totalAmount) == 0)
        }
        if (startsAt != null && endsAt != null) {
            require(!endsAt.isBefore(startsAt)) { "subscription.period.inverted" }
        }
    }
}
