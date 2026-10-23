package pe.edu.upc.viora.features.subscription.infrastructure.remote

import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
data class ProducerSubscriptionDto(
    val id: String,
    val mode: String,
    val planCode: String? = null,
    val status: String,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val quotaHa: BigDecimal,
    val currency: String? = null,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val baseAmount: BigDecimal? = null,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val taxAmount: BigDecimal? = null,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val totalAmount: BigDecimal? = null,
    val startsAt: String? = null,
    val endsAt: String? = null,
    val entitlementActive: Boolean = false,
    val cooperativeId: String? = null,
)
