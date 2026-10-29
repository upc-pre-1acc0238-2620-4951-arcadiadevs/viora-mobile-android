package pe.edu.upc.viora.features.home.presentation.ui

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.PhaseCardArtwork
import pe.edu.upc.viora.core.designsystem.component.PhaseIllustration
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.features.home.presentation.state.HarvestProgress
import pe.edu.upc.viora.features.home.presentation.state.HomePhase
import pe.edu.upc.viora.features.home.presentation.state.HomePhaseUiState
import pe.edu.upc.viora.features.home.presentation.state.PhaseAction
import pe.edu.upc.viora.features.home.presentation.state.PhaseCard
import pe.edu.upc.viora.features.home.presentation.state.PhaseFigure
import pe.edu.upc.viora.features.home.presentation.state.PhaseMessage
import pe.edu.upc.viora.features.home.presentation.state.PhaseMilestone
import pe.edu.upc.viora.features.home.presentation.state.PhaseTiming

/**
 * Where each pill of the carousel leads; every one but the harvest acts on the plot in focus. The harvest pill opens
 * [harvestPicker], the plot picker (P70) harvest settlement draws for the plots still to register; [onDone] closes it.
 */
class HomePhaseActions(
    val onOpenChill: (plotId: String, plotName: String) -> Unit,
    val onOpenForecast: (plotId: String, plotName: String) -> Unit,
    /** The plan of the plot with its P64 full bloom sheet already open. */
    val onRecordBloom: (plotId: String, plotName: String) -> Unit,
    val onSample: (plotId: String, plotName: String) -> Unit,
    val onOpenThinningPlan: (plotId: String, plotName: String) -> Unit,
    val onOpenAlternation: (plotId: String, plotName: String) -> Unit,
    val harvestPicker: @Composable (harvest: HarvestProgress, onDone: () -> Unit) -> Unit,
)

/**
 * Home phase carousel (Figma P10 · Fase): one dark card per phase of the olive year, opened on the phase going on
 * today, with the "Siguiente" tab on its right that names the milestone closing it and moves to the next phase, and
 * the five page dots under it. The harvest pill opens the plot picker (P70).
 */
@Composable
fun HomePhaseCarousel(
    state: HomePhaseUiState,
    actions: HomePhaseActions,
    modifier: Modifier = Modifier,
) {
    if (state.cards.isEmpty()) return
    val pager = rememberPagerState(initialPage = state.current) { state.cards.size }
    val scope = rememberCoroutineScope()
    var picking by rememberSaveable { mutableStateOf(false) }
    // The current phase can move once the harvest data arrives (June: harvest or winter).
    LaunchedEffect(state.current) { pager.scrollToPage(state.current) }

    fun act(card: PhaseCard) {
        val id = state.focusedPlotId
        val name = state.focusedPlotName
        when (card.action) {
            PhaseAction.REGISTER_HARVEST -> picking = true
            else -> if (id != null) when (card.action) {
                PhaseAction.SEE_CHILL -> actions.onOpenChill(id, name)
                PhaseAction.SEE_FORECAST -> actions.onOpenForecast(id, name)
                PhaseAction.RECORD_BLOOM -> actions.onRecordBloom(id, name)
                PhaseAction.SAMPLE -> actions.onSample(id, name)
                PhaseAction.SEE_THINNING_PLAN -> actions.onOpenThinningPlan(id, name)
                PhaseAction.SEE_ALTERNATION -> actions.onOpenAlternation(id, name)
                PhaseAction.REGISTER_HARVEST -> Unit
            }
        }
    }

    Column(modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pager,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 24.dp,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val card = state.cards[page]
            val next = state.cards[(page + 1) % state.cards.size]
            Row(Modifier.fillMaxWidth().height(CardHeight), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhaseCardView(card = card, onAction = { act(card) }, modifier = Modifier.weight(1f))
                NextPhaseTab(
                    card = card,
                    nextPhase = next.phase,
                    onClick = { scope.launch { pager.animateScrollToPage((page + 1) % state.cards.size) } },
                )
            }
        }
        PageDots(count = state.cards.size, selected = pager.currentPage, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp))
    }

    val harvest = state.harvest
    if (picking && harvest != null) {
        actions.harvestPicker(harvest) { picking = false }
    }
}

/** Figma: the card and its "Siguiente" tab are 316 dp tall. */
private val CardHeight = 316.dp

@Composable
private fun PhaseCardView(card: PhaseCard, onAction: () -> Unit, modifier: Modifier = Modifier) {
    val onDark = Neutral50.copy(alpha = 0.78f)
    Box(modifier.height(CardHeight).clip(RoundedCornerShape(32.dp)).background(Green900)) {
        PhaseCardArtwork(card.phase.illustration)
        Column(Modifier.fillMaxSize().padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (card.timing == PhaseTiming.NOW || card.timing == PhaseTiming.LATE) Harvest300 else Neutral300))
                Text(
                    stringResource(R.string.phase_eyebrow, stringResource(card.timing.label), stringResource(card.phase.label)).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.54.sp),
                    color = onDark,
                )
            }
            Figure(card.figure, Modifier.padding(top = 2.dp))
            Text(
                messageText(card.message),
                style = MaterialTheme.typography.bodyMedium,
                color = onDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Box(Modifier.weight(1f))
            Capsule(card, Modifier.padding(top = 10.dp))
            ActionPill(stringResource(card.actionLabel()), onAction, Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun Figure(figure: PhaseFigure, modifier: Modifier = Modifier) {
    val (big, unit) = figureTexts(figure)
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        Text(
            text = big,
            style = MaterialTheme.typography.displayLarge.copy(fontFamily = NewsreaderFamily, fontSize = 96.sp, lineHeight = 96.sp, letterSpacing = (-2.88).sp),
            color = Neutral50,
            maxLines = 1,
        )
        if (unit.isNotEmpty()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 36.sp),
                fontStyle = FontStyle.Italic,
                color = Neutral50,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
    }
}

/** The capsule between the two dated milestones of the phase ("Cuajado · 3 nov" … "Carozo · 30 nov"). */
@Composable
private fun Capsule(card: PhaseCard, modifier: Modifier = Modifier) {
    val fraction = card.progress.coerceIn(0f, 1f)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(18.dp), contentAlignment = Alignment.CenterStart) {
            Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Neutral50Faint))
            if (fraction > 0f) {
                Box(Modifier.fillMaxWidth(fraction).height(12.dp).clip(RoundedCornerShape(6.dp)).background(Harvest300))
                // Figma: an 18 dp knob whose end overlaps the end of the filled part by 6 dp.
                val knobX = (maxWidth * fraction - 12.dp).coerceIn(0.dp, maxWidth - 18.dp)
                Box(
                    Modifier
                        .offset(x = knobX)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Harvest300)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Neutral50),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp)
            Text(milestoneText(card.start, card.startsOn), style = style, color = Neutral50.copy(alpha = 0.6f))
            Text(milestoneText(card.end, card.endsOn), style = style, color = Neutral50.copy(alpha = 0.6f))
        }
    }
}

private val Neutral50Faint = Neutral50.copy(alpha = 0.14f)

@Composable
private fun ActionPill(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Neutral50)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = Green900)
        Box(Modifier.size(36.dp).clip(CircleShape).background(Harvest300), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral900, modifier = Modifier.size(20.dp))
        }
    }
}

/** Figma "Carrusel de fases · Siguiente": a 46 dp tab with the closing milestone written bottom to top. */
@Composable
private fun NextPhaseTab(card: PhaseCard, nextPhase: HomePhase, onClick: () -> Unit) {
    val label = stringResource(R.string.phase_next_tab, stringResource(card.end.label), shortDate(card.endsOn))
    val description = stringResource(R.string.phase_next_description, stringResource(nextPhase.label))
    Box(
        Modifier
            .width(46.dp)
            .height(CardHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(Green200)
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = NewsreaderFamily, fontSize = 17.sp, lineHeight = 22.sp),
            fontStyle = FontStyle.Italic,
            color = Green900,
            maxLines = 1,
            softWrap = false,
            // Measured unbounded, then turned: the text is as long as the tab is tall.
            modifier = Modifier.wrapContentSize(unbounded = true).rotate(-90f),
        )
    }
}

/** Five dots; the selected one is a 20 dp pill (Figma "Indicador de fases"). */
@Composable
private fun PageDots(count: Int, selected: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val width by animateDpAsState(if (index == selected) 20.dp else 6.dp, label = "dot")
            Box(Modifier.width(width).height(6.dp).clip(CircleShape).background(if (index == selected) Neutral700 else Neutral300))
        }
    }
}

private val HomePhase.illustration: PhaseIllustration
    get() = when (this) {
        HomePhase.DORMANCY -> PhaseIllustration.DORMANCY
        HomePhase.BLOOM -> PhaseIllustration.BLOOM
        HomePhase.FRUIT_SET -> PhaseIllustration.FRUIT_SET
        HomePhase.THINNING -> PhaseIllustration.THINNING
        HomePhase.HARVEST -> PhaseIllustration.HARVEST
    }

@get:StringRes
internal val HomePhase.label: Int
    get() = when (this) {
        HomePhase.DORMANCY -> R.string.phase_dormancy
        HomePhase.BLOOM -> R.string.phase_bloom
        HomePhase.FRUIT_SET -> R.string.phase_fruit_set
        HomePhase.THINNING -> R.string.phase_thinning
        HomePhase.HARVEST -> R.string.phase_harvest
    }

@get:StringRes
private val PhaseTiming.label: Int
    get() = when (this) {
        PhaseTiming.NOW -> R.string.phase_timing_now
        PhaseTiming.NEXT -> R.string.phase_timing_next
        PhaseTiming.DONE -> R.string.phase_timing_done
        PhaseTiming.LATE -> R.string.phase_timing_late
    }

@get:StringRes
private val PhaseMilestone.label: Int
    get() = when (this) {
        PhaseMilestone.START -> R.string.phase_milestone_start
        PhaseMilestone.BUDBREAK -> R.string.phase_milestone_budbreak
        PhaseMilestone.BLOOM -> R.string.phase_milestone_bloom
        PhaseMilestone.FRUIT_SET -> R.string.phase_milestone_fruit_set
        PhaseMilestone.THINNING -> R.string.phase_milestone_thinning
        PhaseMilestone.PIT_HARDENING -> R.string.phase_milestone_pit_hardening
        PhaseMilestone.CLOSE -> R.string.phase_milestone_close
    }

@StringRes
private fun PhaseCard.actionLabel(): Int = when (action) {
    PhaseAction.SEE_CHILL -> R.string.phase_action_chill
    PhaseAction.SEE_FORECAST -> R.string.phase_action_forecast
    PhaseAction.RECORD_BLOOM -> R.string.phase_action_record_bloom
    PhaseAction.SAMPLE -> if (hasSamplingStarted) R.string.phase_action_sample_continue else R.string.phase_action_sample_start
    PhaseAction.SEE_THINNING_PLAN -> R.string.phase_action_thinning_plan
    PhaseAction.REGISTER_HARVEST -> R.string.phase_action_harvest
    PhaseAction.SEE_ALTERNATION -> R.string.phase_action_alternation
}

@Composable
private fun milestoneText(milestone: PhaseMilestone, on: LocalDate): String =
    stringResource(R.string.phase_milestone, stringResource(milestone.label), shortDate(on))

/** "30 nov" / "Nov 30": the short month without the abbreviation dot some locales add. */
@Composable
private fun shortDate(on: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateTimeFormatter.ofPattern(stringResource(R.string.phase_date_pattern), locale).format(on).replace(".", "")
}

@Composable
private fun figureTexts(figure: PhaseFigure): Pair<String, String> {
    val locale = LocalConfiguration.current.locales[0]
    return when (figure) {
        is PhaseFigure.Days -> figure.days.toString() to pluralStringResource(R.plurals.phase_unit_days, figure.days)
        is PhaseFigure.ChillPortions -> figure.accumulated.toString() to stringResource(R.string.phase_unit_of, figure.threshold)
        is PhaseFigure.Temperature -> stringResource(R.string.phase_temperature, figure.celsius) to dayName(figure.on, locale)
        is PhaseFigure.Trees -> figure.count.toString() to pluralStringResource(R.plurals.phase_unit_trees, figure.count)
        is PhaseFigure.Plots -> figure.count.toString() to pluralStringResource(R.plurals.phase_unit_plots, figure.count)
        is PhaseFigure.Date -> figure.on.dayOfMonth.toString() to
            DateTimeFormatter.ofPattern(stringResource(R.string.phase_unit_month_pattern), locale).format(figure.on).replace(".", "")
        PhaseFigure.Unknown -> "–" to ""
    }
}

/** "hoy", "mañana" or "el jueves" for the hottest day of the bloom card. */
@Composable
private fun dayName(on: LocalDate, locale: Locale): String {
    val today = LocalDate.now()
    return when (on) {
        today -> stringResource(R.string.phase_day_today)
        today.plusDays(1) -> stringResource(R.string.phase_day_tomorrow)
        else -> stringResource(R.string.phase_day_named, on.dayOfWeek.getDisplayName(TextStyle.FULL, locale))
    }
}

@Composable
private fun messageText(message: PhaseMessage): String = when (message) {
    is PhaseMessage.ChillMissing -> pluralStringResource(R.plurals.phase_msg_chill_missing, message.portions, message.portions)
    PhaseMessage.ChillReached -> stringResource(R.string.phase_msg_chill_reached)
    is PhaseMessage.ChillLastWinter -> stringResource(R.string.phase_msg_chill_last_winter, message.seasonYear)
    is PhaseMessage.ChillNoData -> stringResource(R.string.phase_msg_chill_no_data, message.plot)
    is PhaseMessage.BloomHeat -> stringResource(R.string.phase_msg_bloom_heat, message.plot)
    is PhaseMessage.BloomMild -> stringResource(R.string.phase_msg_bloom_mild, message.plot)
    is PhaseMessage.BloomUntilFruitSet -> stringResource(R.string.phase_msg_bloom_until_fruit_set, message.plot)
    is PhaseMessage.TreesMissing -> stringResource(R.string.phase_msg_trees_missing, message.plot)
    is PhaseMessage.SamplingDone -> stringResource(R.string.phase_msg_sampling_done, message.plot)
    PhaseMessage.BloomMissing -> stringResource(R.string.phase_msg_bloom_missing)
    is PhaseMessage.ThinPercentage -> stringResource(R.string.phase_msg_thin_percentage, message.percentage, message.plot)
    is PhaseMessage.ThinWithoutPlan -> stringResource(R.string.phase_msg_thin_without_plan, message.plot)
    is PhaseMessage.ThinningRecorded -> stringResource(R.string.phase_msg_thinning_recorded, message.plot)
    PhaseMessage.HarvestPending -> stringResource(R.string.phase_msg_harvest_pending)
    is PhaseMessage.HarvestRegistered -> stringResource(R.string.phase_msg_harvest_registered, message.campaignYear)
    is PhaseMessage.StartsIn -> stringResource(
        when (message.phase) {
            HomePhase.DORMANCY -> R.string.phase_msg_starts_dormancy
            HomePhase.BLOOM -> R.string.phase_msg_starts_bloom
            HomePhase.FRUIT_SET -> R.string.phase_msg_starts_fruit_set
            HomePhase.THINNING -> R.string.phase_msg_starts_thinning
            HomePhase.HARVEST -> R.string.phase_msg_starts_harvest
        },
    )
    is PhaseMessage.Ended -> stringResource(
        when (message.phase) {
            HomePhase.DORMANCY -> R.string.phase_msg_ended_dormancy
            HomePhase.BLOOM -> R.string.phase_msg_ended_bloom
            HomePhase.FRUIT_SET -> R.string.phase_msg_ended_fruit_set
            HomePhase.THINNING -> R.string.phase_msg_ended_thinning
            HomePhase.HARVEST -> R.string.phase_msg_ended_harvest
        },
    )
}
