package pe.edu.upc.viora.features.subscription.domain.entity

import java.math.BigDecimal
import java.net.URI
import java.time.Instant
import java.util.Currency
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.core.domain.Uuid

/** Confirmed server terms; this model never grants application access. */
data class IndividualPurchase(
    val id: String,
    val planCode: ProducerPlanCode,
    val capacity: PlanCapacity,
    val currency: String,
    val base: BigDecimal,
    val tax: BigDecimal,
    val total: BigDecimal,
    val status: String,
) {
    init {
        Uuid.parse(id)
        Currency.getInstance(currency)
        require(base.signum() >= 0 && tax.signum() >= 0 && total.signum() > 0)
        require(base.add(tax).compareTo(total) == 0)
    }
}

/** Only exact provider hosts over HTTPS are eligible for external navigation. */
data class IndividualCheckout(val intentId: String, val url: String, val expiresAt: Instant) {
    init {
        Uuid.parse(intentId)
        require(isAllowedCheckoutUrl(url)) { "subscription.checkout.url.invalid" }
    }
}

fun isAllowedCheckoutUrl(value: String): Boolean = runCatching {
    val uri = URI(value)
    uri.scheme == "https" && uri.host in setOf("www.mercadopago.com.pe", "sandbox.mercadopago.com.pe") &&
        uri.rawUserInfo == null && uri.port in setOf(-1, 443) && uri.path.startsWith("/checkout/")
}.getOrDefault(false)
