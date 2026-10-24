package pe.edu.upc.viora.features.home.presentation.state

import java.time.LocalDate
import java.time.MonthDay
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.HarvestEntryUiState
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast

/** The five phases of the olive year the Home carousel walks through (Figma P10 · Fase), in carousel order. */
enum class HomePhase { DORMANCY, BLOOM, FRUIT_SET, THINNING, HARVEST }

/**
 * Where today falls against a phase: the current one, one still ahead or one already behind. [LATE] is a phase
 * already behind with work still open (a harvest not registered after June).
 */
enum class PhaseTiming { NOW, NEXT, DONE, LATE }

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

/** What the Home phase carousel shows. [current] is the page it opens on. */
data class HomePhaseUiState(
    val cards: List<PhaseCard> = emptyList(),
    val current: Int = 0,
    val focusedPlotId: String? = null,
    val focusedPlotName: String = "",
    val harvest: HarvestEntryUiState? = null,
) {
    /** The phase the headline speaks about ("Hoy toca aclarar."), only while it is going on. */
    val headlinePhase: HomePhase? get() = cards.getOrNull(current)?.takeIf { it.timing == PhaseTiming.NOW }?.phase
}

/**
 * The calendar of the olive year in the producer's valley (La Yarada, southern hemisphere) and how today's data
 * fills each phase. The campaign is the calendar year, as everywhere else in the app: the harvest of April-June and
 * the bloom and thinning of September-November of the same year belong to the same campaign. When the plot has its
 * own dates (the recorded full bloom, the thinning window of its plan) they replace the usual ones.
 */
object HomePhaseRules {
    private val DORMANCY_STARTS = MonthDay.of(6, 1)
    private val DORMANCY_ENDS = MonthDay.of(8, 31)
    private val BUDBREAK = MonthDay.of(9, 1)
    private val USUAL_FULL_BLOOM = MonthDay.of(10, 5)
    private val USUAL_WINDOW_OPENS = MonthDay.of(11, 3)
    private val USUAL_WINDOW_CLOSES = MonthDay.of(11, 30)
    private val HARVEST_STARTS = MonthDay.of(4, 20)
    private val HARVEST_ENDS = MonthDay.of(6, 30)

    /** A bloom figure above this temperature asks to water early (heat dries the flower). */
    const val BLOOM_HEAT_CELSIUS = 30.0

    fun state(
        today: LocalDate,
        focused: Plot?,
        plan: CampaignPlan?,
        chill: ChillTracker?,
        forecast: WeatherForecast?,
        harvest: HarvestEntryUiState?,
    ): HomePhaseUiState {
        val year = today.year
        val plot = focused?.name.orEmpty()
        val prescription = plan?.prescription
        val fullBloom = prescription?.fullBloomOn ?: USUAL_FULL_BLOOM.atYear(year)
        val profileWindow = prescription?.fullBloomOn?.let { prescription.windowFor(it) }
        val opens = prescription?.windowOpensOn ?: profileWindow?.first ?: USUAL_WINDOW_OPENS.atYear(year)
        val closes = prescription?.windowClosesOn ?: profileWindow?.second ?: USUAL_WINDOW_CLOSES.atYear(year)

        val cards = listOf(
            dormancy(today, chill, plot),
            bloom(today, fullBloom, forecast, plot, bloomRecorded = prescription?.fullBloomOn != null),
            fruitSet(today, fullBloom, opens, plan, plot),
            thinning(today, opens, closes, plan, plot),
            harvest(today, harvest),
        )
        return HomePhaseUiState(
            cards = cards,
            current = currentIndex(cards, harvest),
            focusedPlotId = focused?.id?.value,
            focusedPlotName = plot,
            harvest = harvest,
        )
    }

    /**
     * The page the carousel opens on: the phase going on today. In June the harvest and the winter overlap; the
     * harvest wins while a plot is still to register. Between two phases (December to April) it is the next one.
     */
    private fun currentIndex(cards: List<PhaseCard>, harvest: HarvestEntryUiState?): Int {
        val now = cards.indices.filter { cards[it].timing == PhaseTiming.NOW }
        val harvestIndex = HomePhase.HARVEST.ordinal
        if (harvestIndex in now && (harvest?.pendingCount ?: 0) > 0) return harvestIndex
        now.firstOrNull { it != harvestIndex }?.let { return it }
        now.firstOrNull()?.let { return it }
        return cards.indices.filter { cards[it].timing == PhaseTiming.NEXT }.minByOrNull { cards[it].startsOn } ?: harvestIndex
    }

    private fun dormancy(today: LocalDate, chill: ChillTracker?, plot: String): PhaseCard {
        val starts = DORMANCY_STARTS.atYear(today.year)
        val ends = DORMANCY_ENDS.atYear(today.year)
        val timing = timing(today, starts, ends)
        val base = PhaseCard(
            HomePhase.DORMANCY, timing, PhaseFigure.Days(daysBetween(today, starts)), PhaseMessage.StartsIn(HomePhase.DORMANCY),
            PhaseMilestone.START, starts, PhaseMilestone.BUDBREAK, ends, 0f, PhaseAction.SEE_CHILL,
        )
        if (timing == PhaseTiming.NEXT) return base
        if (chill == null) {
            return base.copy(figure = PhaseFigure.Unknown, message = PhaseMessage.ChillNoData(plot), progress = elapsed(today, starts, ends))
        }
        val figure = PhaseFigure.ChillPortions(chill.accumulatedPortions.toInt(), chill.thresholdPortions.toInt())
        val progress = if (chill.thresholdPortions > 0) (chill.accumulatedPortions / chill.thresholdPortions).toFloat().coerceIn(0f, 1f) else 0f
        val message = when {
            timing == PhaseTiming.DONE -> PhaseMessage.ChillLastWinter(chill.seasonYear)
            chill.isCompleted -> PhaseMessage.ChillReached
            else -> PhaseMessage.ChillMissing(kotlin.math.ceil(chill.portionsRemaining).toInt())
        }
        return base.copy(figure = figure, message = message, progress = progress)
    }

    private fun bloom(today: LocalDate, fullBloom: LocalDate, forecast: WeatherForecast?, plot: String, bloomRecorded: Boolean): PhaseCard {
        val starts = BUDBREAK.atYear(today.year)
        val timing = timing(today, starts, fullBloom)
        // The full bloom fixes the plot's thinning window: until it is recorded, recording it is the pill.
        val action = if (bloomRecorded) PhaseAction.SEE_FORECAST else PhaseAction.RECORD_BLOOM
        val base = generic(HomePhase.BLOOM, today, timing, PhaseMilestone.BUDBREAK, starts, PhaseMilestone.FRUIT_SET, fullBloom, action)
        if (timing != PhaseTiming.NOW) return base
        val hottest = forecast?.days?.filter { !it.date.isBefore(today) }?.maxByOrNull { it.maxTemperatureCelsius }
            ?: return base.copy(figure = PhaseFigure.Days(daysBetween(today, fullBloom)), message = PhaseMessage.BloomUntilFruitSet(plot))
        val message = if (hottest.maxTemperatureCelsius >= BLOOM_HEAT_CELSIUS) PhaseMessage.BloomHeat(plot) else PhaseMessage.BloomMild(plot)
        return base.copy(figure = PhaseFigure.Temperature(kotlin.math.round(hottest.maxTemperatureCelsius).toInt(), hottest.date), message = message)
    }

    private fun fruitSet(today: LocalDate, fullBloom: LocalDate, opens: LocalDate, plan: CampaignPlan?, plot: String): PhaseCard {
        val timing = timing(today, fullBloom, opens.minusDays(1))
        val base = generic(HomePhase.FRUIT_SET, today, timing, PhaseMilestone.BLOOM, fullBloom, PhaseMilestone.THINNING, opens, PhaseAction.SAMPLE)
        if (timing != PhaseTiming.NOW) return base
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

    private fun thinning(today: LocalDate, opens: LocalDate, closes: LocalDate, plan: CampaignPlan?, plot: String): PhaseCard {
        val timing = timing(today, opens, closes)
        val base = generic(HomePhase.THINNING, today, timing, PhaseMilestone.FRUIT_SET, opens, PhaseMilestone.PIT_HARDENING, closes, PhaseAction.SEE_THINNING_PLAN)
        if (timing != PhaseTiming.NOW) return base
        val percentage = plan?.prescription?.takeIf { it.isIssued }?.percentageToRemove
        val message = when {
            plan?.execution != null -> PhaseMessage.ThinningRecorded(plot)
            percentage != null -> PhaseMessage.ThinPercentage(kotlin.math.round(percentage).toInt(), plot)
            else -> PhaseMessage.ThinWithoutPlan(plot)
        }
        return base.copy(figure = PhaseFigure.Days(daysBetween(today, closes)), message = message)
    }

    private fun harvest(today: LocalDate, harvest: HarvestEntryUiState?): PhaseCard {
        val starts = HARVEST_STARTS.atYear(today.year)
        val ends = HARVEST_ENDS.atYear(today.year)
        val timing = timing(today, starts, ends)
        val pending = harvest?.pendingCount ?: 0
        val total = harvest?.totalPlots ?: 0
        val base = generic(HomePhase.HARVEST, today, timing, PhaseMilestone.START, starts, PhaseMilestone.CLOSE, ends, PhaseAction.REGISTER_HARVEST)
        val registeredShare = if (total > 0) (total - pending).toFloat() / total else 0f
        return when {
            // A plot still to register is news whatever the date: the producer may register late.
            pending > 0 && timing != PhaseTiming.NEXT -> base.copy(
                timing = if (timing == PhaseTiming.DONE) PhaseTiming.LATE else timing,
                figure = PhaseFigure.Plots(pending),
                message = PhaseMessage.HarvestPending,
                progress = registeredShare,
            )
            timing != PhaseTiming.NEXT && total > 0 ->
                base.copy(figure = PhaseFigure.Plots(total), message = PhaseMessage.HarvestRegistered(today.year), progress = 1f, action = PhaseAction.SEE_ALTERNATION)
            else -> base
        }
    }

    /** A card with no data of its own: the days until it starts, how long ago it ended, or the share of it elapsed. */
    private fun generic(
        phase: HomePhase, today: LocalDate, timing: PhaseTiming,
        start: PhaseMilestone, startsOn: LocalDate, end: PhaseMilestone, endsOn: LocalDate, action: PhaseAction,
    ): PhaseCard {
        val (figure, message) = when (timing) {
            PhaseTiming.NEXT -> PhaseFigure.Days(daysBetween(today, startsOn)) to PhaseMessage.StartsIn(phase)
            PhaseTiming.DONE -> PhaseFigure.Date(endsOn) to PhaseMessage.Ended(phase)
            PhaseTiming.NOW, PhaseTiming.LATE -> PhaseFigure.Days(daysBetween(today, endsOn)) to PhaseMessage.StartsIn(phase)
        }
        return PhaseCard(phase, timing, figure, message, start, startsOn, end, endsOn, elapsed(today, startsOn, endsOn), action)
    }

    private fun timing(today: LocalDate, starts: LocalDate, ends: LocalDate): PhaseTiming = when {
        today.isBefore(starts) -> PhaseTiming.NEXT
        today.isAfter(ends) -> PhaseTiming.DONE
        else -> PhaseTiming.NOW
    }

    private fun elapsed(today: LocalDate, starts: LocalDate, ends: LocalDate): Float {
        val total = ChronoUnit.DAYS.between(starts, ends).coerceAtLeast(1)
        return (ChronoUnit.DAYS.between(starts, today).toFloat() / total).coerceIn(0f, 1f)
    }

    private fun daysBetween(from: LocalDate, to: LocalDate): Int = ChronoUnit.DAYS.between(from, to).toInt().coerceAtLeast(0)

}
