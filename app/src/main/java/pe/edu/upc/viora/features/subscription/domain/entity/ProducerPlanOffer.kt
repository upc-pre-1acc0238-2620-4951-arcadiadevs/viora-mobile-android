package pe.edu.upc.viora.features.subscription.domain.entity

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode

/** Commercial conditions read from the server; an estimate is never proof of a purchase. */
data class ProducerPlanOffer(
    val code: ProducerPlanCode,
    val currency: String,
    val monthlyRatePerHa: BigDecimal,
    val taxRate: BigDecimal,
    val minimumQuota: PlanCapacity,
    val quotaIncrement: PlanCapacity,
    val billingMonths: Int,
    val tariffVersion: String,
    val taxIncluded: Boolean,
) {
    init {
        require(currency.matches(Regex("[A-Z]{3}"))) { "subscription.currency.invalid" }
        Currency.getInstance(currency)
        require(monthlyRatePerHa.signum() >= 0) { "subscription.rate.invalid" }
        require(taxRate >= BigDecimal.ZERO && taxRate <= BigDecimal.ONE) { "subscription.tax.invalid" }
        require(billingMonths > 0) { "subscription.billing.invalid" }
        require(tariffVersion.isNotBlank()) { "subscription.tariff.blank" }
        require(minimumQuota.hectares.remainder(quotaIncrement.hectares).signum() == 0) {
            "subscription.capacity.increment.invalid"
        }
    }

    fun accepts(capacity: PlanCapacity): Boolean = capacity.hectares >= minimumQuota.hectares &&
        capacity.hectares.remainder(quotaIncrement.hectares).signum() == 0

    /** Mirrors server rounding for display only; checkout must use the server's confirmed total. */
    fun estimate(capacity: PlanCapacity): PlanEstimate {
        require(accepts(capacity)) { "subscription.capacity.invalid" }
        val monthlyBase = monthlyRatePerHa.multiply(capacity.hectares)
        val annualRate = monthlyBase.multiply(billingMonths.toBigDecimal()).setScale(2, RoundingMode.HALF_UP)
        // Retain explicit legacy catalog semantics; never assume a missing remote flag is inclusive.
        val base = if (taxIncluded) annualRate.divide(BigDecimal.ONE.add(taxRate), 2, RoundingMode.HALF_UP) else annualRate
        val tax = if (taxIncluded) annualRate.subtract(base) else base.multiply(taxRate).setScale(2, RoundingMode.HALF_UP)
        val total = base.add(tax)
        return PlanEstimate(base, tax, total, total.divide(billingMonths.toBigDecimal(), 2, RoundingMode.HALF_UP))
    }
}

/** Exact estimated breakdown in the currency of its offer. */
data class PlanEstimate(val base: BigDecimal, val tax: BigDecimal, val total: BigDecimal, val monthlyTotal: BigDecimal)
