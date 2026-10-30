package pe.edu.upc.viora.features.subscription.infrastructure.mapper

import java.time.Instant
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualCheckout
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualPurchase
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.infrastructure.remote.IndividualPurchaseDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.IndividualCheckoutDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerSubscriptionDto

fun IndividualPurchaseDto.toDomain(): IndividualPurchase {
    require(mode == "PLAN_PRODUCTOR_INDIVIDUAL") { "subscription.mode.invalid" }
    return IndividualPurchase(id, ProducerPlanCode(requireNotNull(planCode)), PlanCapacity.of(quotaHa),
        requireNotNull(currency), requireNotNull(baseAmount), requireNotNull(taxAmount), requireNotNull(totalAmount), status)
}

fun IndividualCheckoutDto.toDomain() = IndividualCheckout(intentId, checkoutUrl, Instant.parse(expiresAt))

/** The current subscription read from `GET /subscriptions`, as the purchase contract the journal keeps. */
fun ProducerSubscriptionDto.asPurchase(): IndividualPurchaseDto = IndividualPurchaseDto(
    id = id,
    mode = mode,
    planCode = planCode,
    status = status,
    quotaHa = quotaHa,
    currency = currency,
    baseAmount = baseAmount,
    taxAmount = taxAmount,
    totalAmount = totalAmount,
    startsAt = startsAt,
    endsAt = endsAt,
    entitlementActive = entitlementActive,
    cooperativeId = cooperativeId,
)
