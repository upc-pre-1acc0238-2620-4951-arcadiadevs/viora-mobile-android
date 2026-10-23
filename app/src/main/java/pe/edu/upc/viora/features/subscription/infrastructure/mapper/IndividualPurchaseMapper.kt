package pe.edu.upc.viora.features.subscription.infrastructure.mapper

import java.time.Instant
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualCheckout
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualPurchase
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.infrastructure.remote.IndividualPurchaseDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.IndividualCheckoutDto

fun IndividualPurchaseDto.toDomain(): IndividualPurchase {
    require(mode == "PLAN_PRODUCTOR_INDIVIDUAL") { "subscription.mode.invalid" }
    return IndividualPurchase(id, ProducerPlanCode(requireNotNull(planCode)), PlanCapacity.of(quotaHa),
        requireNotNull(currency), requireNotNull(baseAmount), requireNotNull(taxAmount), requireNotNull(totalAmount), status)
}

fun IndividualCheckoutDto.toDomain() = IndividualCheckout(intentId, checkoutUrl, Instant.parse(expiresAt))
