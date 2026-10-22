package pe.edu.upc.viora.features.subscription.infrastructure

import java.math.BigDecimal
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import pe.edu.upc.viora.features.subscription.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerPlanOfferDto

internal val catalogFixture = """[{"code":"PRODUCER_ANNUAL","currency":"PEN","monthlyRatePerHa":55,
    "taxRate":0.18,"minimumQuotaHa":0.5,"quotaIncrementHa":0.5,"billingMonths":12,"tariffVersion":"fixture-1"}]"""

class ProducerPlanOfferMapperTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun decode(value: String = catalogFixture) = json.decodeFromString<List<ProducerPlanOfferDto>>(value).single()

    @Test fun `all commercial conditions come from the numeric response`() {
        val plan = decode().toDomain()
        assertEquals("PRODUCER_ANNUAL", plan.code.value)
        assertEquals("PEN", plan.currency)
        assertEquals(BigDecimal("55"), plan.monthlyRatePerHa)
        assertEquals(BigDecimal("0.18"), plan.taxRate)
        assertEquals(BigDecimal("0.5"), plan.minimumQuota.hectares)
        assertEquals(12, plan.billingMonths)
        assertEquals("fixture-1", plan.tariffVersion)
    }

    @Test fun `decimal precision survives decoding and encoding without double`() {
        val input = catalogFixture.replace(":55,", ":55.1234567890123456789,")
        val dto = decode(input)
        assertEquals(BigDecimal("55.1234567890123456789"), dto.monthlyRatePerHa)
        assertEquals(dto, decode(json.encodeToString(listOf(dto))))
    }

    @Test fun `a missing commercial field cannot silently become a default tariff`() {
        assertThrows(SerializationException::class.java) { decode(catalogFixture.replace("\"taxRate\":0.18,", "")) }
    }

    @Test fun `invalid and non-finite values cannot become an offer`() {
        assertThrows(SerializationException::class.java) { decode(catalogFixture.replace(":55,", ":\"NaN\",")) }
        assertThrows(IllegalArgumentException::class.java) { decode(catalogFixture.replace(":0.5,", ":0,")).toDomain() }
    }

    @Test fun `unknown future fields do not change the quoted conditions`() {
        val input = catalogFixture.replace("\"currency\":", "\"description\":\"future field\",\"currency\":")
        assertEquals(decode().toDomain(), decode(input).toDomain())
    }
}
