package pe.edu.upc.viora.features.croploadregulation.presentation.state

import java.time.Instant
import java.time.LocalDate
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.WindowPhase
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionStatus
import pe.edu.upc.viora.features.plotmanagement.application.published.Plot

/** P60 · Plan: every plot with the state of its thinning plan for one campaign, the urgent ones first. */
data class CampaignPlanUiState(
    val campaignYear: Int,
    val campaignYears: List<Int>,
    val today: LocalDate,
    val rows: List<PlanRow> = emptyList(),
    val hasPlots: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val error: AppError? = null,
    val lastRefresh: Instant? = null,
    val hasUnreadAlerts: Boolean = false,
) {
    val isCurrentCampaign: Boolean
        get() = campaignYear == today.year

    /** Plots whose window is open and still need thinning: the block "N lotes necesitan aclareo esta semana". */
    val urgent: List<PlanRow>
        get() = rows.filter { it.status == PlanRowStatus.RECOMMENDED && it.phase is WindowPhase.Open }

    /** The next window to open when nothing is urgent, to say when the next thinning comes. */
    val nextOpening: PlanRow?
        get() = rows.filter { it.status == PlanRowStatus.WINDOW_NOT_OPEN }.minByOrNull { it.prescription?.windowOpensOn ?: LocalDate.MAX }
}

/** One plot of P60 with what its plan says today. */
data class PlanRow(
    val plot: Plot,
    val status: PlanRowStatus,
    val prescription: ThinningPrescription?,
    val execution: ThinningExecution?,
    val phase: WindowPhase?,
) {
    /** Days left before the pit hardens, only while the window is open. */
    val daysLeft: Int?
        get() = (phase as? WindowPhase.Open)?.daysLeft
}

/** The pill of each P60 row, in the order the list shows them: what needs a hand now goes first. */
enum class PlanRowStatus {
    /** Window open, fruit to remove: "Aclareo recomendado". */
    RECOMMENDED,

    /** Issued, window still closed ahead: "Ventana por abrir". */
    WINDOW_NOT_OPEN,

    /** Five trees sampled, the full bloom is missing (P64): "Falta floración". */
    NO_BLOOM,

    /** Fewer than five trees: "Muestra incompleta". */
    INCOMPLETE_SAMPLE,

    /** No approved target for the variety: "Sin perfil técnico". */
    NO_PROFILE,

    /** Nothing sampled this campaign: "Sin muestreo". */
    NO_SAMPLING,

    /** The pit hardened without a recorded thinning: "Ventana concluida". */
    WINDOW_CLOSED,

    /** Thinning recorded: "Aclareo registrado". */
    EXECUTED,

    /** The load is already sustainable: "Carga óptima". */
    BALANCED,
}

object CampaignPlanRules {

    /** The rows of every plot, a plot without a cached plan being one never sampled, sorted by urgency then name. */
    fun rows(plots: List<Plot>, plans: List<CampaignPlan>, today: LocalDate): List<PlanRow> {
        val byPlot = plans.associateBy { it.plotId }
        return plots.map { plot ->
            val plan = byPlot[plot.id.value]
            val prescription = plan?.prescription
            PlanRow(
                plot = plot,
                status = statusOf(prescription, today),
                prescription = prescription,
                execution = plan?.execution,
                phase = prescription?.windowPhase(today),
            )
        }.sortedWith(compareBy<PlanRow> { it.status.ordinal }.thenBy { it.daysLeft ?: Int.MAX_VALUE }.thenBy { it.plot.name })
    }

    fun statusOf(prescription: ThinningPrescription?, today: LocalDate): PlanRowStatus {
        val plan = prescription ?: return PlanRowStatus.NO_SAMPLING
        if (plan.status == PrescriptionStatus.EXECUTED) return PlanRowStatus.EXECUTED
        if (plan.isIssued) {
            val percentage = plan.percentageToRemove ?: 0.0
            if (percentage <= 0.0 || plan.currentLoad?.loadState == LoadState.BALANCED) return PlanRowStatus.BALANCED
            return when (plan.windowPhase(today)) {
                is WindowPhase.NotOpenYet -> PlanRowStatus.WINDOW_NOT_OPEN
                is WindowPhase.Closed -> PlanRowStatus.WINDOW_CLOSED
                is WindowPhase.Open, null -> PlanRowStatus.RECOMMENDED
            }
        }
        return when {
            PrescriptionBlocker.SAMPLING_NOT_REPRESENTATIVE in plan.blockers ->
                if (plan.sampledTrees == 0) PlanRowStatus.NO_SAMPLING else PlanRowStatus.INCOMPLETE_SAMPLE
            PrescriptionBlocker.TARGET_NOT_CONFIGURED in plan.blockers -> PlanRowStatus.NO_PROFILE
            PrescriptionBlocker.FULL_BLOOM_MISSING in plan.blockers -> PlanRowStatus.NO_BLOOM
            else -> PlanRowStatus.INCOMPLETE_SAMPLE
        }
    }

    /** The campaigns the chip offers: this one and the two before, where past plans can still be read. */
    fun campaignYears(today: LocalDate): List<Int> = (0..2).map { today.year - it }
}
