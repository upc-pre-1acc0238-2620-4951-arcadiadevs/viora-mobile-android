package pe.edu.upc.viora.features.subscription.domain

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus

class ProducerSubscriptionTest {

    private val validId = UUID.randomUUID().toString()
    private val validCapacity = PlanCapacity.of(BigDecimal("0.5"))

    @Test
    fun `creates valid active individual subscription`() {
        val starts = Instant.parse("2026-10-22T00:00:00Z")
        val ends = Instant.parse("2027-10-22T00:00:00Z")
        val subscription = ProducerSubscription(
            id = validId,
            mode = SubscriptionMode.INDIVIDUAL,
            planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
            status = SubscriptionStatus.ACTIVE,
            capacity = validCapacity,
            currency = "PEN",
            baseAmount = BigDecimal("279.66"),
            taxAmount = BigDecimal("50.34"),
            totalAmount = BigDecimal("330.00"),
            startsAt = starts,
            endsAt = ends,
            entitlementActive = true,
            cooperativeId = null,
        )

        assertEquals(validId, subscription.id)
        assertEquals(SubscriptionMode.INDIVIDUAL, subscription.mode)
        assertEquals(SubscriptionStatus.ACTIVE, subscription.status)
        assertTrue(subscription.entitlementActive)
        assertEquals(BigDecimal("330.00"), subscription.totalAmount)
    }

    @Test
    fun `creates valid pending individual subscription without dates or entitlement`() {
        val subscription = ProducerSubscription(
            id = validId,
            mode = SubscriptionMode.INDIVIDUAL,
            planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
            status = SubscriptionStatus.PENDING_PAYMENT,
            capacity = validCapacity,
            currency = "PEN",
            baseAmount = BigDecimal("279.66"),
            taxAmount = BigDecimal("50.34"),
            totalAmount = BigDecimal("330.00"),
            startsAt = null,
            endsAt = null,
            entitlementActive = false,
            cooperativeId = null,
        )

        assertEquals(SubscriptionStatus.PENDING_PAYMENT, subscription.status)
        assertFalse(subscription.entitlementActive)
        assertNull(subscription.startsAt)
        assertNull(subscription.endsAt)
    }

    @Test
    fun `creates valid cooperative sponsored subscription without payment amounts`() {
        val starts = Instant.parse("2026-10-22T00:00:00Z")
        val ends = Instant.parse("2027-10-22T00:00:00Z")
        val coopId = UUID.randomUUID().toString()
        val subscription = ProducerSubscription(
            id = validId,
            mode = SubscriptionMode.COOPERATIVE_SPONSORED,
            planCode = null,
            status = SubscriptionStatus.ACTIVE,
            capacity = validCapacity,
            currency = null,
            baseAmount = null,
            taxAmount = null,
            totalAmount = null,
            startsAt = starts,
            endsAt = ends,
            entitlementActive = true,
            cooperativeId = coopId,
        )

        assertEquals(SubscriptionMode.COOPERATIVE_SPONSORED, subscription.mode)
        assertNull(subscription.planCode)
        assertNull(subscription.totalAmount)
        assertEquals(coopId, subscription.cooperativeId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects invalid UUID`() {
        ProducerSubscription(
            id = "not-a-uuid",
            mode = SubscriptionMode.INDIVIDUAL,
            planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
            status = SubscriptionStatus.PENDING_PAYMENT,
            capacity = validCapacity,
            currency = "PEN",
            baseAmount = BigDecimal("279.66"),
            taxAmount = BigDecimal("50.34"),
            totalAmount = BigDecimal("330.00"),
            startsAt = null,
            endsAt = null,
            entitlementActive = false,
            cooperativeId = null,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects mismatched tax base total amounts`() {
        ProducerSubscription(
            id = validId,
            mode = SubscriptionMode.INDIVIDUAL,
            planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
            status = SubscriptionStatus.PENDING_PAYMENT,
            capacity = validCapacity,
            currency = "PEN",
            baseAmount = BigDecimal("200.00"),
            taxAmount = BigDecimal("50.00"),
            totalAmount = BigDecimal("330.00"), // 200 + 50 != 330
            startsAt = null,
            endsAt = null,
            entitlementActive = false,
            cooperativeId = null,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects inverted period dates`() {
        ProducerSubscription(
            id = validId,
            mode = SubscriptionMode.INDIVIDUAL,
            planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
            status = SubscriptionStatus.ACTIVE,
            capacity = validCapacity,
            currency = "PEN",
            baseAmount = BigDecimal("279.66"),
            taxAmount = BigDecimal("50.34"),
            totalAmount = BigDecimal("330.00"),
            startsAt = Instant.parse("2027-10-22T00:00:00Z"),
            endsAt = Instant.parse("2026-10-22T00:00:00Z"),
            entitlementActive = true,
            cooperativeId = null,
        )
    }

    @Test
    fun `SubscriptionMode parses standard and platform string codes`() {
        assertEquals(SubscriptionMode.INDIVIDUAL, SubscriptionMode.from("INDIVIDUAL"))
        assertEquals(SubscriptionMode.INDIVIDUAL, SubscriptionMode.from("PLAN_PRODUCTOR_INDIVIDUAL"))
        assertEquals(SubscriptionMode.COOPERATIVE_SPONSORED, SubscriptionMode.from("COOPERATIVE_SPONSORED"))
        assertEquals(SubscriptionMode.COOPERATIVE_SPONSORED, SubscriptionMode.from("PLAN_PRODUCTOR_COOPERATIVA"))
        assertEquals(SubscriptionMode.UNKNOWN, SubscriptionMode.from("OTHER"))
        assertEquals(SubscriptionMode.UNKNOWN, SubscriptionMode.from(null))
    }

    @Test
    fun `SubscriptionStatus parses standard string states`() {
        assertEquals(SubscriptionStatus.PENDING_PAYMENT, SubscriptionStatus.from("PENDING_PAYMENT"))
        assertEquals(SubscriptionStatus.ACTIVE, SubscriptionStatus.from("ACTIVE"))
        assertEquals(SubscriptionStatus.EXPIRED, SubscriptionStatus.from("EXPIRED"))
        assertEquals(SubscriptionStatus.CANCELLED, SubscriptionStatus.from("CANCELLED"))
        assertEquals(SubscriptionStatus.UNKNOWN, SubscriptionStatus.from("OTHER"))
        assertEquals(SubscriptionStatus.UNKNOWN, SubscriptionStatus.from(null))
    }

    @Test
    fun `the id follows the shared kernel uuid, not any text java accepts`() {
        fun subscription(id: String) = ProducerSubscription(
            id = id, mode = SubscriptionMode.INDIVIDUAL, planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
            status = SubscriptionStatus.ACTIVE, capacity = validCapacity, currency = "PEN", baseAmount = null,
            taxAmount = null, totalAmount = null, startsAt = null, endsAt = null, entitlementActive = true, cooperativeId = null,
        )
        // java.util.UUID.fromString takes "1-1-1-1-1"; the backend never issues it.
        assertThrows(IllegalArgumentException::class.java) { subscription("1-1-1-1-1") }
        subscription(validId.uppercase())
    }
}
