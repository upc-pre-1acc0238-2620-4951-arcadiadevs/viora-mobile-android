package pe.edu.upc.viora.features.croploadregulation.presentation

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.croploadregulation.blockedPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.WindowPhase
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.croploadregulation.issuedPlan
import pe.edu.upc.viora.features.croploadregulation.presentation.state.PlanStage
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ThinningPlanUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.state.excessPercent
import pe.edu.upc.viora.features.croploadregulation.presentation.state.removalFraction

class ThinningPlanRulesTest {

    private fun content(plan: ThinningPrescription?) = ThinningPlanUiState.Content(
        plotName = "La Yarada 02",
        variety = null,
        areaHectares = 2.5,
        campaignYear = 2026,
        prescription = plan,
        today = LocalDate.of(2026, 11, 18),
        syncedAt = Instant.EPOCH,
        isRefreshing = false,
        offline = false,
    )

    @Test
    fun `the share to remove reads as the simplest fraction of ten fruits at most`() {
        assertEquals(3 to 10, removalFraction(30.0))
        assertEquals(1 to 6, removalFraction(17.0))
        assertEquals(1 to 2, removalFraction(50.0))
        assertEquals(1 to 4, removalFraction(25.0))
        assertEquals(3 to 7, removalFraction(43.0))
        assertNull(removalFraction(0.0))
        assertNull(removalFraction(3.0))
    }

    @Test
    fun `the excess over the target is whole percent and never negative`() {
        assertEquals(43, excessPercent(1.43))
        assertEquals(20, excessPercent(1.2))
        assertEquals(0, excessPercent(0.95))
    }

    @Test
    fun `each missing input picks its variant, sampling first`() {
        assertEquals(PlanStage.SAMPLING, content(null).stage)
        assertEquals(
            PlanStage.SAMPLING,
            content(blockedPlan(PrescriptionBlocker.SAMPLING_NOT_REPRESENTATIVE, PrescriptionBlocker.FULL_BLOOM_MISSING, sampled = 3)).stage,
        )
        assertEquals(PlanStage.NO_PROFILE, content(blockedPlan(PrescriptionBlocker.TARGET_NOT_CONFIGURED, PrescriptionBlocker.FULL_BLOOM_MISSING)).stage)
        assertEquals(PlanStage.NO_BLOOM, content(blockedPlan(PrescriptionBlocker.FULL_BLOOM_MISSING)).stage)
        assertEquals(PlanStage.PRESCRIBED, content(issuedPlan()).stage)
    }

    @Test
    fun `nothing sampled means five trees missing`() {
        val state = content(null)
        assertEquals(0, state.sampledTrees)
        assertEquals(5, state.treesNeeded)
        assertEquals(2, content(blockedPlan(PrescriptionBlocker.SAMPLING_NOT_REPRESENTATIVE, sampled = 3)).treesNeeded)
    }

    @Test
    fun `today against the window`() {
        val plan = issuedPlan()
        assertEquals(WindowPhase.NotOpenYet(2), plan.windowPhase(LocalDate.of(2026, 11, 1)))
        val open = plan.windowPhase(LocalDate.of(2026, 11, 18)) as WindowPhase.Open
        assertEquals(12, open.daysLeft)
        assertEquals(15f / 27f, open.progress, 0.001f)
        assertEquals(WindowPhase.Open(0, 1f), plan.windowPhase(LocalDate.of(2026, 11, 30)))
        assertEquals(WindowPhase.Closed(9), plan.windowPhase(LocalDate.of(2026, 12, 9)))
        assertNull(blockedPlan(PrescriptionBlocker.FULL_BLOOM_MISSING).windowPhase(LocalDate.of(2026, 11, 18)))
    }

    @Test
    fun `the profile offsets preview the window of any bloom date`() {
        val plan = blockedPlan(PrescriptionBlocker.FULL_BLOOM_MISSING)
        assertEquals(LocalDate.of(2026, 11, 3) to LocalDate.of(2026, 11, 30), plan.windowFor(LocalDate.of(2026, 10, 20)))
        assertNull(plan.copy(windowOpensDaysAfterBloom = null).windowFor(LocalDate.of(2026, 10, 20)))
    }
}
