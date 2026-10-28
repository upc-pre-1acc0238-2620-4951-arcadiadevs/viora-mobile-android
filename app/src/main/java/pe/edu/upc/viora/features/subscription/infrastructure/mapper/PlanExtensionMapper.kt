package pe.edu.upc.viora.features.subscription.infrastructure.mapper

import java.math.BigDecimal
import java.time.Instant
import pe.edu.upc.viora.features.subscription.domain.entity.ExtensionAction
import pe.edu.upc.viora.features.subscription.domain.entity.ExtensionPricing
import pe.edu.upc.viora.features.subscription.domain.entity.ExtensionStatus
import pe.edu.upc.viora.features.subscription.domain.entity.PlanExtensionTerms
import pe.edu.upc.viora.features.subscription.domain.entity.QuotaExtension
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ExtensionTermsDto
import pe.edu.upc.viora.features.subscription.infrastructure.remote.QuotaExtensionDto

fun ExtensionTermsDto.toDomain(): PlanExtensionTerms {
    val action = ExtensionAction.entries.find { it.name == action } ?: ExtensionAction.NONE
    val pricing = if (action == ExtensionAction.PAY) {
        val months = requireNotNull(billableMonths)
        val monthlyRate = requireNotNull(monthlyRatePerHa)
        ExtensionPricing(
            billableMonths = months,
            monthlyRatePerHa = monthlyRate,
            currency = requireNotNull(currency),
            minimum = requireNotNull(minimumHa),
            increment = requireNotNull(incrementHa),
            billableDays = billableDays,
            ratePerHa = extensionRatePerHa ?: monthlyRate.multiply(BigDecimal(months)),
        )
    } else null
    return PlanExtensionTerms(subscriptionId, action, quotaHa, endsAt?.let(Instant::parse), pricing)
}

fun QuotaExtensionDto.toDomain() = QuotaExtension(
    id = id,
    subscriptionId = subscriptionId,
    status = if (status == "APPLIED") ExtensionStatus.APPLIED else ExtensionStatus.PENDING_PAYMENT,
    addedHectares = addedHa,
    quotaHectares = quotaHa,
    endsAt = endsAt?.let(Instant::parse),
    billedMonths = billedMonths,
    currency = currency,
    total = totalAmount,
    paymentStatus = paymentStatus,
)
