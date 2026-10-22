package pe.edu.upc.viora.features.subscription.infrastructure.mapper

import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanOffer
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.infrastructure.local.PRODUCER_PLAN_CATALOG_KEY
import pe.edu.upc.viora.features.subscription.infrastructure.local.ProducerPlanEntity

fun ProducerPlanOffer.toEntity() = ProducerPlanEntity(
    code.value, PRODUCER_PLAN_CATALOG_KEY, currency, monthlyRatePerHa.toPlainString(), taxRate.toPlainString(),
    minimumQuota.hectares.toPlainString(), quotaIncrement.hectares.toPlainString(), billingMonths, tariffVersion,
)

fun ProducerPlanEntity.toDomain() = ProducerPlanOffer(
    ProducerPlanCode(code), currency, monthlyRatePerHa.toBigDecimal(), taxRate.toBigDecimal(),
    PlanCapacity.of(minimumQuotaHa.toBigDecimal()), PlanCapacity.of(quotaIncrementHa.toBigDecimal()),
    billingMonths, tariffVersion,
)
