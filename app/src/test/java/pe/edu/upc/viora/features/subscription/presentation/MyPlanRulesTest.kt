package pe.edu.upc.viora.features.subscription.presentation

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus
import pe.edu.upc.viora.features.subscription.presentation.state.MyPlanRules
import pe.edu.upc.viora.features.subscription.presentation.state.PlanStanding

/** P99 · Mi plan: the ring, the validity and the variants come from the plan and the active plots. */
class MyPlanRulesTest {

    private val today = LocalDate.of(2026, 10, 24)

    private fun plot(name: String, hectares: Double, active: Boolean = true) = Plot(
        id = PlotId(UUID.randomUUID().toString()), name = name, variety = OliveVariety.SEVILLANA, areaHectares = hectares,
        treesPerHectare = 72, rowSpacingMeters = 7.0, treeSpacingMeters = 5.0,
        outline = listOf(GeoPoint(-18.05, -70.25), GeoPoint(-18.05, -70.24), GeoPoint(-18.04, -70.24)),
        lastPruningDate = null, isActive = active, revision = 0,
    )

    private fun plan(endsOn: LocalDate, mode: SubscriptionMode = SubscriptionMode.INDIVIDUAL, active: Boolean = true) = ProducerSubscription(
        id = UUID.randomUUID().toString(),
        mode = mode,
        planCode = null,
        status = if (active) SubscriptionStatus.ACTIVE else SubscriptionStatus.EXPIRED,
        capacity = PlanCapacity.of(BigDecimal("12")),
        currency = null, baseAmount = null, taxAmount = null, totalAmount = null,
        startsAt = endsOn.minusDays(365).atStartOfDay().toInstant(ZoneOffset.UTC),
        endsAt = endsOn.atStartOfDay().toInstant(ZoneOffset.UTC),
        entitlementActive = active,
        cooperativeId = null,
    )

    @Test
    fun `an active plan counts the free hectares of the active plots only`() {
        val plots = listOf(plot("La Yarada 02", 2.5), plot("Lote Norte", 2.4), plot("Archivado", 3.0, active = false))
        val state = MyPlanRules.state(plan(LocalDate.of(2027, 10, 21)), plots, today, ZoneOffset.UTC)

        assertEquals(PlanStanding.ACTIVE, state.standing)
        assertEquals(12.0, state.quotaHectares, 1e-9)
        assertEquals(4.9, state.usedHectares, 1e-9)
        assertEquals(7.1, state.freeHectares, 1e-9)
        assertEquals(2, state.plots.size)
        assertEquals(362, state.daysLeft)
        assertEquals(2, state.plotsThatFit)
        assertEquals(180, state.plots.first().trees)
        assertFalse(state.activatedWithCode)
    }

    @Test
    fun `within thirty days of the end the plan asks to be renewed`() {
        val state = MyPlanRules.state(plan(today.plusDays(12)), emptyList(), today, ZoneOffset.UTC)

        assertEquals(PlanStanding.EXPIRING, state.standing)
        assertEquals(12, state.daysLeft)
    }

    @Test
    fun `a plan activated with a code is told apart`() {
        val state = MyPlanRules.state(plan(LocalDate.of(2027, 10, 21), SubscriptionMode.COOPERATIVE_SPONSORED), emptyList(), today, ZoneOffset.UTC)

        assertTrue(state.activatedWithCode)
    }

    @Test
    fun `without an active plan the plots are kept but there is no quota`() {
        val plots = listOf(plot("La Yarada 02", 2.5))
        val ended = MyPlanRules.state(plan(LocalDate.of(2026, 10, 1), active = false), plots, today, ZoneOffset.UTC)
        val never = MyPlanRules.state(null, plots, today, ZoneOffset.UTC)

        for (state in listOf(ended, never)) {
            assertEquals(PlanStanding.NONE, state.standing)
            assertEquals(0.0, state.quotaHectares, 1e-9)
            assertEquals(0, state.daysLeft)
            assertEquals(2.5, state.usedHectares, 1e-9)
        }
    }

    @Test
    fun `plots registered over a smaller plan count as excess, not as free hectares`() {
        val plots = listOf(plot("Cuartel San Jerónimo", 117.82), plot("Lote Norte", 4.68))
        val state = MyPlanRules.state(plan(LocalDate.of(2027, 10, 21)), plots, today, ZoneOffset.UTC)

        assertTrue(state.isOverQuota)
        assertEquals(0.0, state.freeHectares, 1e-9)
        assertEquals(110.5, state.excessHectares, 1e-9)
    }

    @Test
    fun `the plots list shows the quota only below one free hectare or over the quota`() {
        val ends = LocalDate.of(2027, 10, 21)
        assertFalse(MyPlanRules.state(plan(ends), listOf(plot("A", 11.0)), today, ZoneOffset.UTC).isQuotaTight)
        assertTrue(MyPlanRules.state(plan(ends), listOf(plot("A", 11.5)), today, ZoneOffset.UTC).isQuotaTight)
        assertTrue(MyPlanRules.state(plan(ends), listOf(plot("A", 13.0)), today, ZoneOffset.UTC).isQuotaTight)
        assertFalse(MyPlanRules.state(plan(ends, active = false), listOf(plot("A", 13.0)), today, ZoneOffset.UTC).isQuotaTight)
    }
}
