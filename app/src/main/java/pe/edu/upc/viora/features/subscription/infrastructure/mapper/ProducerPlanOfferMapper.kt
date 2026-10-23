package pe.edu.upc.viora.features.subscription.infrastructure.mapper

import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanOffer
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerPlanOfferDto

/** Rejects incomplete or inconsistent policies before they replace a valid local catalog. */
fun ProducerPlanOfferDto.toDomain() = ProducerPlanOffer(
    code = ProducerPlanCode(code), currency = currency, monthlyRatePerHa = monthlyRatePerHa,
    taxRate = taxRate, minimumQuota = PlanCapacity.of(minimumQuotaHa),
    quotaIncrement = PlanCapacity.of(quotaIncrementHa), billingMonths = billingMonths, tariffVersion = tariffVersion,
    taxIncluded = taxIncluded,
)
