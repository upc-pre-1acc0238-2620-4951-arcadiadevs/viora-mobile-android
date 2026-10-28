package pe.edu.upc.viora.features.subscription.domain

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ExtensionTermsDto

/** The preview total must be the cent the backend charges. */
class ExtensionPricingTest {

    private fun terms(days: Int?, rate: String?) = ExtensionTermsDto(
        subscriptionId = "plan-1", mode = "PLAN_PRODUCTOR_INDIVIDUAL", extendable = true, action = "PAY",
        quotaHa = BigDecimal("12"), billableMonths = 10, monthlyRatePerHa = BigDecimal("55"), currency = "PEN",
        minimumHa = BigDecimal("0.5"), incrementHa = BigDecimal("0.5"), billableDays = days,
        extensionRatePerHa = rate?.let(::BigDecimal),
    ).toDomain().pricing!!

    @Test
    fun pricesByTheDaysLeftWithTheRateTheBackendPublishes() {
        // 306 days left of a 366-day plan: 660 × 306 / 366 = 551.803279 per ha; 3 ha → 1655.41
        val pricing = terms(306, "551.803279")

        assertEquals(306, pricing.billableDays)
        assertEquals(BigDecimal("1655.41"), pricing.total(BigDecimal("3")))
    }

    @Test
    fun anOlderBackendStillPricesByWholeMonths() {
        val pricing = terms(null, null)

        assertNull(pricing.billableDays)
        assertEquals(BigDecimal("1650.00"), pricing.total(BigDecimal("3")))
    }
}
