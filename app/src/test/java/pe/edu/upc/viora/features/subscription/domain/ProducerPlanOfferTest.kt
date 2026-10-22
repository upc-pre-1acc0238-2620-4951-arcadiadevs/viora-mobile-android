package pe.edu.upc.viora.features.subscription.domain

import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanOffer
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode

class ProducerPlanOfferTest {
    private fun capacity(value: String) = PlanCapacity.of(BigDecimal(value))
    private fun offer(rate: String = "55", tax: String = "0.18", minimum: String = "0.5", step: String = "0.5", included: Boolean = false) =
        ProducerPlanOffer(ProducerPlanCode("PRODUCER_ANNUAL"), "PEN", BigDecimal(rate), BigDecimal(tax),
            capacity(minimum), capacity(step), 12, "test-tariff", included)

    @Test fun `inclusive annual prices preserve exact totals and extract tax without monthly rounding`() {
        val cases = listOf(
            listOf("0.5", "330.00", "279.66", "50.34"),
            listOf("1", "660.00", "559.32", "100.68"),
            listOf("10", "6600.00", "5593.22", "1006.78"),
            listOf("12", "7920.00", "6711.86", "1208.14"),
        )
        cases.forEach { (quota, total, base, tax) ->
            val price = offer(included = true).estimate(capacity(quota))
            assertEquals(BigDecimal(total), price.total)
            assertEquals(BigDecimal(base), price.base)
            assertEquals(BigDecimal(tax), price.tax)
            assertEquals(price.total, price.base.add(price.tax))
            assertEquals(BigDecimal(total).divide(BigDecimal("12")), price.monthlyTotal)
        }
    }

    @Test fun `inclusive price uses configured tariff and rate and supports zero tax`() {
        val custom = offer(rate = "70", tax = "0.10", minimum = "0.25", step = "0.25", included = true)
            .estimate(capacity("0.25"))
        assertEquals(BigDecimal("210.00"), custom.total)
        assertEquals(BigDecimal("190.91"), custom.base)
        assertEquals(BigDecimal("19.09"), custom.tax)
        val untaxed = offer(tax = "0", included = true).estimate(capacity("10"))
        assertEquals(untaxed.total, untaxed.base)
        assertEquals(BigDecimal("0.00"), untaxed.tax)
    }

    @Test fun `half a hectare includes the agreed tax exactly once`() {
        val estimate = offer().estimate(capacity("0.5"))
        assertEquals(BigDecimal("330.00"), estimate.base)
        assertEquals(BigDecimal("59.40"), estimate.tax)
        assertEquals(BigDecimal("389.40"), estimate.total)
    }

    @Test fun `twelve hectares use a taxable base rather than the reference price as final total`() {
        assertEquals(BigDecimal("9345.60"), offer().estimate(capacity("12")).total)
    }

    @Test fun `the backend policy changes both rate and increment without mobile constants`() {
        val offer = offer(rate = "70", tax = "0.10", minimum = "0.25", step = "0.25")
        assertEquals(BigDecimal("231.00"), offer.estimate(capacity("0.25")).total)
        assertTrue(offer.accepts(capacity("0.75")))
    }

    @Test fun `capacity must meet the minimum and the exact step`() {
        assertFalse(offer().accepts(capacity("0.1")))
        assertFalse(offer().accepts(capacity("0.75")))
        assertTrue(offer().accepts(capacity("1.50")))
    }

    @Test fun `tax is rounded half up after the annual base`() {
        val estimate = offer(rate = "0.01", tax = "0.125").estimate(capacity("0.5"))
        assertEquals(BigDecimal("0.06"), estimate.base)
        assertEquals(BigDecimal("0.01"), estimate.tax)
        assertEquals(BigDecimal("0.07"), estimate.total)
    }

    @Test fun `decimal scale does not change capacity identity`() {
        assertEquals(capacity("0.5"), capacity("0.500"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero capacity is rejected`() { capacity("0") }

    @Test(expected = IllegalArgumentException::class)
    fun `a negative tariff is not an offer`() { offer(rate = "-1") }

    @Test(expected = IllegalArgumentException::class)
    fun `a minimum off the step cannot produce an impossible selector`() { offer(minimum = "0.6") }

    @Test(expected = IllegalArgumentException::class)
    fun `an invalid capacity cannot be priced`() { offer().estimate(capacity("0.75")) }
}
