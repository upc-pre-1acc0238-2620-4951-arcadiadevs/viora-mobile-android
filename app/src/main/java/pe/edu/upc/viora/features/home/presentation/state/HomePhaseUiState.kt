package pe.edu.upc.viora.features.home.presentation.state

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.features.croploadregulation.application.published.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.application.published.ThinningPrescription
import pe.edu.upc.viora.features.harvestsettlement.application.published.PlotToSettle
import pe.edu.upc.viora.features.phenology.application.published.CurrentPhase
import pe.edu.upc.viora.features.phenology.application.published.OlivePhase
import pe.edu.upc.viora.features.phenology.application.published.PhaseWindow
import pe.edu.upc.viora.features.phenology.application.published.PlotPhaseDates
import pe.edu.upc.viora.features.plotmanagement.application.published.Plot
import pe.edu.upc.viora.features.telemetry.application.published.WeatherForecast

/** The five phases of the olive year the Home carousel walks through (Figma P10 · Fase), in carousel order. */
typealias HomePhase = OlivePhase

/** Where today falls against a phase; phenology decides it. */
typealias PhaseTiming = pe.edu.upc.viora.features.phenology.application.published.PhaseTiming

/** The events that bound a phase on its progress capsule ("Cuajado · 3 nov", "Carozo · 30 nov"). */
enum class PhaseMilestone { START, BUDBREAK, BLOOM, FRUIT_SET, THINNING, PIT_HARDENING, CLOSE }

/** The big figure of a card and what it reads ("12 días", "24 de 30", "34° el jueves"). */
sealed interface PhaseFigure {
    data class Days(val days: Int) : PhaseFigure
    data class ChillPortions(val accumulated: Int, val threshold: Int) : PhaseFigure
    data class Temperature(val celsius: Int, val on: LocalDate) : PhaseFigure
    data class Trees(val count: Int) : PhaseFigure
    data class Plots(val count: Int) : PhaseFigure
    data class Date(val on: LocalDate) : PhaseFigure
    /** Nothing measured yet (a plot without chill data): the card shows a dash. */
    data object Unknown : PhaseFigure
}

/** The sentence under the figure; each one names the plot in focus when it is about a plot. */
sealed interface PhaseMessage {
    data class ChillMissing(val portions: Int) : PhaseMessage
    data object ChillReached : PhaseMessage
    data class ChillLastWinter(val seasonYear: Int) : PhaseMessage
    data class ChillNoData(val plot: String) : PhaseMessage
    data class BloomHeat(val plot: String) : PhaseMessage
    data class BloomMild(val plot: String) : PhaseMessage
    data class BloomUntilFruitSet(val plot: String) : PhaseMessage
    data class TreesMissing(val plot: String) : PhaseMessage
    data class SamplingDone(val plot: String) : PhaseMessage
    data object BloomMissing : PhaseMessage
    data class ThinPercentage(val percentage: Int, val plot: String) : PhaseMessage
    data class ThinWithoutPlan(val plot: String) : PhaseMessage
    data class ThinningRecorded(val plot: String) : PhaseMessage
    data object HarvestPending : PhaseMessage
    data class HarvestRegistered(val campaignYear: Int) : PhaseMessage
    data class StartsIn(val phase: HomePhase) : PhaseMessage
    data class Ended(val phase: HomePhase) : PhaseMessage
}

/** Where the card's pill leads. */
enum class PhaseAction { SEE_CHILL, SEE_FORECAST, RECORD_BLOOM, SAMPLE, SEE_THINNING_PLAN, REGISTER_HARVEST, SEE_ALTERNATION }

/** One page of the carousel: a phase bounded by two dated milestones, its figure, its sentence and its pill. */
data class PhaseCard(
    val phase: HomePhase,
    val timing: PhaseTiming,
    val figure: PhaseFigure,
    val message: PhaseMessage,
    val start: PhaseMilestone,
    val startsOn: LocalDate,
    val end: PhaseMilestone,
    val endsOn: LocalDate,
    /** Filled part of the capsule, 0..1. */
    val progress: Float,
    val action: PhaseAction,
    /** False when the pill would lead nowhere useful (sampling on a plot whose sampling is complete). */
    val hasSamplingStarted: Boolean = false,
)

/** The harvest card's data and its picker's (P70): the plots of [campaignYear] still to register out of [totalPlots]. */
data class HarvestProgress(val campaignYear: Int, val plots: List<PlotToSettle>, val totalPlots: Int) {
    val pendingCount: Int get() = plots.size
}

/** What the Home phase carousel shows. [current] is the page it opens on. */
data class HomePhaseUiState(
    val cards: List<PhaseCard> = emptyList(),
    val current: Int = 0,
    val focusedPlotId: String? = null,
    val focusedPlotName: String = "",
    val harvest: HarvestProgress? = null,
) {
    /** The phase the headline speaks about ("Hoy toca aclarar."), only while it is going on. */
    val headlinePhase: HomePhase? get() = cards.getOrNull(current)?.takeIf { it.timing == PhaseTiming.NOW }?.phase
}

/**
 * How the plot's data fills each phase of the olive year on the Home carousel. Phenology decides the phases, their
 * dates and the one going on ([CurrentPhase]); this only turns them into the figure, sentence and pill of each card.
 */
object HomePhaseRules {

    /** What the other contexts know that moves the plot's phases: its full bloom, its thinning window, the harvests. */
    fun phaseDates(plan: CampaignPlan?, harvest: HarvestProgress?): PlotPhaseDates {
        val prescription = plan?.prescription
        val profileWindow = prescription?.fullBloomOn?.let { prescription.windowFor(it) }
        return PlotPhaseDates(
            fullBloomOn = prescription?.fullBloomOn,
            thinningOpensOn = prescription?.windowOpensOn ?: profileWindow?.first,
            thinningClosesOn = prescription?.windowClosesOn ?: profileWindow?.second,
            harvestPending = (harvest?.pendingCount ?: 0) > 0,
        )
    }

    fun state(
        phase: CurrentPhase,
        focused: Plot?,
        plan: CampaignPlan?,
        forecast: WeatherForecast?,
        harvest: HarvestProgress?,
    ): HomePhaseUiState {
        val plot = focused?.name.orEmpty()
        val today = phase.today
        val cards = listOf(
            dormancy(today, phase, plot),
            bloom(today, phase, forecast, plot),
            fruitSet(today, phase.window(HomePhase.FRUIT_SET), plan, plot),
            thinning(today, phase.window(HomePhase.THINNING), plan, plot),
            harvest(today, phase.window(HomePhase.HARVEST), harvest),
        )
        return HomePhaseUiState(
            cards = cards,
            current = phase.current.ordinal,
            focusedPlotId = focused?.id?.value,
            focusedPlotName = plot,
            harvest = harvest,
        )
    }

    private fun dormancy(today: LocalDate, phase: CurrentPhase, plot: String): PhaseCard {
        val window = phase.window(HomePhase.DORMANCY)
        val base = PhaseCard(
            HomePhase.DORMANCY, window.timing, PhaseFigure.Days(daysBetween(today, window.startsOn)), PhaseMessage.StartsIn(HomePhase.DORMANCY),
            PhaseMilestone.START, window.startsOn, PhaseMilestone.BUDBREAK, window.endsOn, window.progress, PhaseAction.SEE_CHILL,
        )
        if (window.timing == PhaseTiming.NEXT) return base
        val chill = phase.chill ?: return base.copy(figure = PhaseFigure.Unknown, message = PhaseMessage.ChillNoData(plot))
        val figure = PhaseFigure.ChillPortions(chill.accumulatedPortions.toInt(), chill.thresholdPortions.toInt())
        val message = when {
            window.timing == PhaseTiming.DONE -> PhaseMessage.ChillLastWinter(chill.seasonYear)
            chill.isCompleted -> PhaseMessage.ChillReached
            else -> PhaseMessage.ChillMissing(kotlin.math.ceil(chill.portionsRemaining).toInt())
        }
        return base.copy(figure = figure, message = message)
    }

    private fun bloom(today: LocalDate, phase: CurrentPhase, forecast: WeatherForecast?, plot: String): PhaseCard {
        val window = phase.window(HomePhase.BLOOM)
        // The full bloom fixes the plot's thinning window: until it is recorded, recording it is the pill.
        val action = if (phase.bloomRecorded) PhaseAction.SEE_FORECAST else PhaseAction.RECORD_BLOOM
        val base = generic(today, window, PhaseMilestone.BUDBREAK, PhaseMilestone.FRUIT_SET, action)
        if (window.timing != PhaseTiming.NOW) return base
        val hottest = forecast?.days?.filter { !it.date.isBefore(today) }?.maxByOrNull { it.maxTemperatureCelsius }
            ?: return base.copy(figure = PhaseFigure.Days(daysBetween(today, window.endsOn)), message = PhaseMessage.BloomUntilFruitSet(plot))
        val message = if (phase.isBloomHeat(hottest.maxTemperatureCelsius)) PhaseMessage.BloomHeat(plot) else PhaseMessage.BloomMild(plot)
        return base.copy(figure = PhaseFigure.Temperature(kotlin.math.round(hottest.maxTemperatureCelsius).toInt(), hottest.date), message = message)
    }

    private fun fruitSet(today: LocalDate, window: PhaseWindow, plan: CampaignPlan?, plot: String): PhaseCard {
        val opens = window.endsOn
        val base = generic(today, window, PhaseMilestone.BLOOM, PhaseMilestone.THINNING, PhaseAction.SAMPLE)
        if (window.timing != PhaseTiming.NOW) return base
        val prescription = plan?.prescription
        val sampled = prescription?.sampledTrees ?: 0
        // The backend's treesNeeded is what is still missing for a representative round, not the total.
        val missing = (prescription?.treesNeeded ?: ThinningPrescription.SAMPLE_SIZE).coerceAtLeast(0)
        return if (missing > 0) {
            base.copy(figure = PhaseFigure.Trees(missing), message = PhaseMessage.TreesMissing(plot), hasSamplingStarted = sampled > 0)
        } else if (prescription?.fullBloomOn == null) {
            // Sampled but the window is still the usual one: only the full bloom makes it the plot's own.
            base.copy(figure = PhaseFigure.Days(daysBetween(today, opens)), message = PhaseMessage.BloomMissing, action = PhaseAction.RECORD_BLOOM)
        } else {
            base.copy(figure = PhaseFigure.Days(daysBetween(today, opens)), message = PhaseMessage.SamplingDone(plot), action = PhaseAction.SEE_THINNING_PLAN)
        }
    }

    private fun thinning(today: LocalDate, window: PhaseWindow, plan: CampaignPlan?, plot: String): PhaseCard {
        val base = generic(today, window, PhaseMilestone.FRUIT_SET, PhaseMilestone.PIT_HARDENING, PhaseAction.SEE_THINNING_PLAN)
        if (window.timing != PhaseTiming.NOW) return base
        val percentage = plan?.prescription?.takeIf { it.isIssued }?.percentageToRemove
        val message = when {
            plan?.execution != null -> PhaseMessage.ThinningRecorded(plot)
            percentage != null -> PhaseMessage.ThinPercentage(kotlin.math.round(percentage).toInt(), plot)
            else -> PhaseMessage.ThinWithoutPlan(plot)
        }
        return base.copy(figure = PhaseFigure.Days(daysBetween(today, window.endsOn)), message = message)
    }

    private fun harvest(today: LocalDate, window: PhaseWindow, harvest: HarvestProgress?): PhaseCard {
        val pending = harvest?.pendingCount ?: 0
        val total = harvest?.totalPlots ?: 0
        val base = generic(today, window, PhaseMilestone.START, PhaseMilestone.CLOSE, PhaseAction.REGISTER_HARVEST)
        val registeredShare = if (total > 0) (total - pending).toFloat() / total else 0f
        return when {
            // A plot still to register is news whatever the date: the producer may register late.
            pending > 0 && window.timing != PhaseTiming.NEXT -> base.copy(
                figure = PhaseFigure.Plots(pending),
                message = PhaseMessage.HarvestPending,
                progress = registeredShare,
            )
            window.timing != PhaseTiming.NEXT && total > 0 ->
                base.copy(figure = PhaseFigure.Plots(total), message = PhaseMessage.HarvestRegistered(today.year), progress = 1f, action = PhaseAction.SEE_ALTERNATION)
            else -> base
        }
    }

    /** A card with no data of its own: the days until it starts, how long ago it ended, or the share of it elapsed. */
    private fun generic(today: LocalDate, window: PhaseWindow, start: PhaseMilestone, end: PhaseMilestone, action: PhaseAction): PhaseCard {
        val (figure, message) = when (window.timing) {
            PhaseTiming.NEXT -> PhaseFigure.Days(daysBetween(today, window.startsOn)) to PhaseMessage.StartsIn(window.phase)
            PhaseTiming.DONE -> PhaseFigure.Date(window.endsOn) to PhaseMessage.Ended(window.phase)
            PhaseTiming.NOW, PhaseTiming.LATE -> PhaseFigure.Days(daysBetween(today, window.endsOn)) to PhaseMessage.StartsIn(window.phase)
        }
        return PhaseCard(window.phase, window.timing, figure, message, start, window.startsOn, end, window.endsOn, window.progress, action)
    }

    private fun daysBetween(from: LocalDate, to: LocalDate): Int = ChronoUnit.DAYS.between(from, to).toInt().coerceAtLeast(0)
}
