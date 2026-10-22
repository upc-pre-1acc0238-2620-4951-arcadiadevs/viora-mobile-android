package pe.edu.upc.viora.features.subscription.infrastructure.remote

import java.math.BigDecimal
import kotlinx.serialization.Serializable

/** Required catalog fields from GET subscription-plans; independent of redemption DTOs. */
@Serializable
data class ProducerPlanOfferDto(
    val code: String,
    val currency: String,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val monthlyRatePerHa: BigDecimal,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val taxRate: BigDecimal,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val minimumQuotaHa: BigDecimal,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val quotaIncrementHa: BigDecimal,
    val billingMonths: Int,
    val tariffVersion: String,
    val taxIncluded: Boolean,
)
