package pe.edu.upc.viora.features.subscription.infrastructure.mapper

import java.time.Instant
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerSubscriptionDto

fun ProducerSubscriptionDto.toDomain(): ProducerSubscription = ProducerSubscription(
    id = id,
    mode = SubscriptionMode.from(mode),
    planCode = planCode?.takeIf { it.isNotBlank() }?.let { ProducerPlanCode(it) },
    status = SubscriptionStatus.from(status),
    capacity = PlanCapacity.of(quotaHa),
    currency = currency,
    baseAmount = baseAmount,
    taxAmount = taxAmount,
    totalAmount = totalAmount,
    startsAt = startsAt?.takeIf { it.isNotBlank() }?.let { Instant.parse(it) },
    endsAt = endsAt?.takeIf { it.isNotBlank() }?.let { Instant.parse(it) },
    entitlementActive = entitlementActive,
    cooperativeId = cooperativeId?.takeIf { it.isNotBlank() },
    paymentStatus = pe.edu.upc.viora.features.subscription.domain.valueobject.IndividualPaymentStatus.from(paymentStatus),
)
