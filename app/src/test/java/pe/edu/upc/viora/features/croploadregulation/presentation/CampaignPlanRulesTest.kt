package pe.edu.upc.viora.features.croploadregulation.presentation

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.croploadregulation.blockedPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionStatus
import pe.edu.upc.viora.features.croploadregulation.issuedPlan
import pe.edu.upc.viora.features.croploadregulation.presentation.state.CampaignPlanRules
import pe.edu.upc.viora.features.croploadregulation.presentation.state.CampaignPlanUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.state.PlanRowStatus
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

class CampaignPlanRulesTest {

    /** The window of [issuedPlan] runs from 3 to 30 November 2026. */
    private val inWindow = LocalDate.of(2026, 11, 18)

    private fun plot(id: String) = Plot(
        id = PlotId(id),
        name = id,
        variety = OliveVariety.SEVILLANA,
        areaHectares = 2.5,
        treesPerHectare = 286,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 5.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 0,
        registeredAt = Instant.parse("2026-11-18T10:00:00Z"),
    )

    private fun status(plan: ThinningPrescription?, today: LocalDate = inWindow) = CampaignPlanRules.statusOf(plan, today)

    @Test
    fun `each plan gets the pill of its state`() {
        assertEquals(PlanRowStatus.NO_SAMPLING, status(null))
        assertEquals(PlanRowStatus.RECOMMENDED, status(issuedPlan()))
        assertEquals(PlanRowStatus.WINDOW_NOT_OPEN, status(issuedPlan(), LocalDate.of(2026, 11, 1)))
        assertEquals(PlanRowStatus.WINDOW_CLOSED, status(issuedPlan(), LocalDate.of(2026, 12, 9)))
        assertEquals(PlanRowStatus.BALANCED, status(issuedPlan(percentage = 0.0, loadState = LoadState.BALANCED)))
        assertEquals(PlanRowStatus.EXECUTED, status(issuedPlan().copy(status = PrescriptionStatus.EXECUTED)))
        assertEquals(PlanRowStatus.INCOMPLETE_SAMPLE, status(blockedPlan(PrescriptionBlocker.SAMPLING_NOT_REPRESENTATIVE, sampled = 3)))
        assertEquals(PlanRowStatus.NO_SAMPLING, status(blockedPlan(PrescriptionBlocker.SAMPLING_NOT_REPRESENTATIVE, sampled = 0)))
        assertEquals(
            PlanRowStatus.NO_PROFILE,
            status(blockedPlan(PrescriptionBlocker.TARGET_NOT_CONFIGURED, PrescriptionBlocker.FULL_BLOOM_MISSING)),
        )
        assertEquals(PlanRowStatus.NO_BLOOM, status(blockedPlan(PrescriptionBlocker.FULL_BLOOM_MISSING)))
    }

    @Test
    fun `the plots that need a hand go first, the closest to the pit on top`() {
        val plots = listOf(plot("done"), plot("never"), plot("late-window"), plot("early-window"), plot("sampling"))
        val plans = listOf(
            CampaignPlan("done", issuedPlan().copy(status = PrescriptionStatus.EXECUTED), null),
            CampaignPlan("late-window", issuedPlan(closes = LocalDate.of(2026, 11, 30)), null),
            CampaignPlan("early-window", issuedPlan(closes = LocalDate.of(2026, 11, 20)), null),
            CampaignPlan("sampling", blockedPlan(PrescriptionBlocker.SAMPLING_NOT_REPRESENTATIVE, sampled = 3), null),
        )

        val rows = CampaignPlanRules.rows(plots, plans, inWindow)

        assertEquals(listOf("early-window", "late-window", "sampling", "never", "done"), rows.map { it.plot.id.value })
        val state = CampaignPlanUiState(campaignYear = 2026, campaignYears = listOf(2026), today = inWindow, rows = rows)
        assertEquals(listOf("early-window", "late-window"), state.urgent.map { it.plot.id.value })
    }

    @Test
    fun `the chip offers this campaign and the two before`() {
        assertEquals(listOf(2026, 2025, 2024), CampaignPlanRules.campaignYears(inWindow))
    }
}
