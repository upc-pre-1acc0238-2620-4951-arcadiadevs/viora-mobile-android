package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.component.VioraLoadingState
import pe.edu.upc.viora.core.designsystem.component.VioraOfflineBanner
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.component.VioraTopBar
import pe.edu.upc.viora.core.designsystem.component.VioraTopBarTitle
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green700
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.newsreader
import pe.edu.upc.viora.core.designsystem.theme.roboto
import pe.edu.upc.viora.core.designsystem.theme.robotoMedium
import pe.edu.upc.viora.core.navigation.LotSection
import pe.edu.upc.viora.core.navigation.LotSectionsSheet
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.core.presentation.formatHectares
import pe.edu.upc.viora.core.presentation.formatWhole
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CurrentLoad
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.WindowPhase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.YieldEstimate
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningProfileStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateBasis
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateStatus
import pe.edu.upc.viora.features.croploadregulation.presentation.state.PlanStage
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ThinningPlanUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.state.excessPercent
import pe.edu.upc.viora.features.croploadregulation.presentation.state.removalFraction
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.ThinningPlanViewModel
import pe.edu.upc.viora.core.presentation.labelRes

/**
 * P61 · Plan del lote (US26-US28): the fruit load the sampling measured against the sustainable target, and the
 * thinning prescription with its window. While an input is missing the plan says which one and how to give it
 * (the sampling, the technical profile or the full bloom of P64), over the prescription card dimmed.
 */
@Composable
fun ThinningPlanScreen(
    onBack: () -> Unit,
    onContinueSampling: (plotName: String) -> Unit,
    onRegisterThinning: (plotName: String, campaignYear: Int) -> Unit,
    onViewResult: (prescriptionId: String, plotName: String, campaignYear: Int) -> Unit,
    onOpenSection: (LotSection, plotName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThinningPlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheet by viewModel.sheet.collectAsStateWithLifecycle()
    var showMore by rememberSaveable { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val content = state as? ThinningPlanUiState.Content

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding(),
    ) {
        TopBar(
            title = content?.plotName.orEmpty(),
            subtitle = content?.let { subtitle(it) }.orEmpty(),
            onBack = onBack,
            onMore = { showMore = true },
        )
        when (val s = state) {
            ThinningPlanUiState.Loading -> VioraLoadingState(modifier = Modifier.weight(1f).fillMaxWidth())
            is ThinningPlanUiState.Error -> MessageBody(
                message = stringResource(s.error.messageRes()),
                onRetry = viewModel::refresh,
                modifier = Modifier.weight(1f),
            )
            is ThinningPlanUiState.Content -> PlanBody(
                state = s,
                scroll = scroll,
                onContinueSampling = { onContinueSampling(s.plotName) },
                onRecordBloom = viewModel::openFullBloomSheet,
                onBack = onBack,
                onRegisterThinning = { onRegisterThinning(s.plotName, s.campaignYear) },
                onViewResult = { id -> onViewResult(id, s.plotName, s.campaignYear) },
                onViewLoad = { scope.launch { scroll.animateScrollTo(0) } },
                modifier = Modifier.weight(1f),
            )
        }
    }

    sheet?.let { bloom ->
        content?.let {
            FullBloomSheet(
                state = bloom,
                plotName = it.plotName,
                campaignYear = it.campaignYear,
                window = it.prescription?.windowFor(bloom.date),
                varietyName = it.variety?.let { variety -> stringResource(variety.labelRes()) },
                onStep = viewModel::stepFullBloom,
                onSave = viewModel::saveFullBloom,
                onDismiss = viewModel::dismissFullBloomSheet,
            )
        }
    }

    if (showMore && content != null) {
        LotSectionsSheet(
            plotName = content.plotName,
            summary = subtitle(content),
            current = LotSection.THINNING,
            onSelect = { section ->
                showMore = false
                onOpenSection(section, content.plotName)
            },
            onDismiss = { showMore = false },
        )
    }
}

@Composable
private fun subtitle(state: ThinningPlanUiState.Content): String {
    val variety = state.variety?.let { stringResource(it.labelRes()) } ?: stringResource(R.string.thinning_this_variety)
    return state.areaHectares?.let {
        stringResource(R.string.thinning_plan_subtitle, state.campaignYear, variety, formatHectares(it))
    } ?: stringResource(R.string.thinning_plan_subtitle_no_area, state.campaignYear, variety)
}

@Composable
private fun TopBar(title: String, subtitle: String, onBack: () -> Unit, onMore: () -> Unit) {
    VioraTopBar(
        onBack = onBack,
        onMore = onMore,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
    ) {
        VioraTopBarTitle(title, subtitle, titleStyle = robotoMedium(15.sp, 20.sp), subtitleStyle = roboto(12.sp, 16.sp))
    }
}

@Composable
private fun PlanBody(
    state: ThinningPlanUiState.Content,
    scroll: ScrollState,
    onContinueSampling: () -> Unit,
    onRecordBloom: () -> Unit,
    onBack: () -> Unit,
    onRegisterThinning: () -> Unit,
    onViewResult: (prescriptionId: String) -> Unit,
    onViewLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val plan = state.prescription
    Column(
        modifier = modifier.verticalScroll(scroll).padding(horizontal = 24.dp).padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (state.offline) OfflineBanner(state.syncedAt)
        when (state.stage) {
            PlanStage.SAMPLING -> BlockedCard(
                eyebrow = stringResource(R.string.thinning_sampling_eyebrow),
                title = if (state.treesNeeded > 0) {
                    pluralStringResource(R.plurals.thinning_sampling_title, state.treesNeeded, state.treesNeeded)
                } else {
                    stringResource(R.string.thinning_sampling_title_more)
                },
                filledSegments = state.sampledTrees,
                body = stringResource(R.string.thinning_sampling_body, ThinningPrescription.SAMPLE_SIZE, state.plotName),
                action = stringResource(if (state.sampledTrees == 0) R.string.thinning_sampling_start else R.string.thinning_sampling_continue),
                onAction = onContinueSampling,
            )
            PlanStage.NO_PROFILE -> BlockedCard(
                eyebrow = stringResource(R.string.thinning_profile_eyebrow),
                title = stringResource(R.string.thinning_profile_title),
                filledSegments = state.sampledTrees,
                body = stringResource(
                    R.string.thinning_profile_body,
                    state.variety?.let { stringResource(it.labelRes()) } ?: stringResource(R.string.thinning_this_variety),
                ),
                action = stringResource(R.string.thinning_profile_action),
                onAction = onBack,
            )
            PlanStage.NO_BLOOM -> BlockedCard(
                eyebrow = stringResource(R.string.thinning_bloom_eyebrow),
                title = stringResource(R.string.thinning_bloom_title),
                filledSegments = state.sampledTrees,
                body = stringResource(R.string.thinning_bloom_body, state.plotName),
                action = stringResource(R.string.thinning_bloom_action),
                onAction = onRecordBloom,
            )
            PlanStage.PRESCRIBED -> plan?.currentLoad?.let { LoadCard(plan, it, state) }
        }
        if (state.stage == PlanStage.PRESCRIBED && plan != null) {
            PrescriptionCard(
                plan = plan,
                state = state,
                onRegister = onRegisterThinning,
                onViewResult = { onViewResult(plan.id) },
                onViewLoad = onViewLoad,
            )
        } else {
            GhostPrescriptionCard()
        }
        Spacer(Modifier.height(VioraTabBarDefaults.ContentBottomPadding))
    }
}

/** "Sin conexión · calculado el 18 nov, 8:40 a. m." over the saved plan. */
@Composable
private fun OfflineBanner(syncedAt: Instant) {
    val locale = LocalConfiguration.current.locales[0]
    val computedAt = DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_synced), locale)
        .format(syncedAt.atZone(LocalAppZone.current)).replace(".,", ",")
    VioraOfflineBanner(
        text = stringResource(R.string.thinning_offline, computedAt),
        iconCircle = 36.dp,
        spacing = 10.dp,
        contentPadding = PaddingValues(6.dp),
        textStyle = robotoMedium(13.sp, 18.sp),
    )
}

/** The yellow card that says which input is missing, with the trees sampled out of five and the way to give it. */
@Composable
private fun BlockedCard(
    eyebrow: String,
    title: String,
    filledSegments: Int,
    body: String,
    action: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Harvest100)
            .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(eyebrow, style = robotoMedium(11.sp, 16.sp).copy(letterSpacing = 1.32.sp), color = Harvest800)
        Text(title, style = newsreader(32.sp, 36.sp).copy(letterSpacing = (-0.48).sp), color = Neutral900)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(ThinningPrescription.SAMPLE_SIZE) { index ->
                Box(
                    Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (index < filledSegments) Green900 else Neutral0),
                )
            }
        }
        Text(body, style = roboto(14.sp, 20.sp), color = Neutral700)
        ArrowPill(text = action, dark = true, onClick = onAction)
    }
}

@Composable
private fun LoadCard(plan: ThinningPrescription, load: CurrentLoad, state: ThinningPlanUiState.Content) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Neutral0)
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.thinning_load_title), style = newsreader(26.sp, 30.sp).copy(letterSpacing = (-0.26).sp), color = Neutral900)
            LoadStateChip(load.loadState)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.thinning_fruits_per_shoot), style = robotoMedium(11.sp, 16.sp).copy(letterSpacing = 1.32.sp), color = Neutral600)
            if (plan.profileStatus == ThinningProfileStatus.SYNTHETIC_DEMO) {
                Text(
                    stringResource(R.string.thinning_demo_target),
                    style = robotoMedium(11.sp, 14.sp),
                    color = Harvest800,
                    modifier = Modifier.clip(CircleShape).background(Harvest100).padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        // The track is the target over 0.7, so a load 43 % above the target fills it (Figma P61).
        val scale = load.targetFruitsPerShoot / TARGET_SHARE_OF_TRACK
        val estimatedFill = loadFill(load.loadState)
        LoadBar(
            label = stringResource(R.string.thinning_load_estimated),
            value = formatDecimal(load.fruitsPerShoot, 2),
            fraction = (load.fruitsPerShoot / scale).toFloat(),
            fill = estimatedFill,
            onFill = if (load.loadState == LoadState.MODERATE_OVERLOAD) Green900 else Neutral50,
        )
        LoadBar(
            label = stringResource(R.string.thinning_load_target),
            value = formatDecimal(load.targetFruitsPerShoot, 2),
            fraction = TARGET_SHARE_OF_TRACK.toFloat(),
            fill = Green800,
            onFill = Neutral50,
        )
        YieldTiles(plan.yieldEstimate)
        LoadReason(load, state.campaignYear)
        plan.lastSampledOn?.let { sampled ->
            val locale = LocalConfiguration.current.locales[0]
            Text(
                pluralStringResource(
                    R.plurals.thinning_based_on,
                    plan.sampledTrees,
                    plan.sampledTrees,
                    DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_day_month), locale).format(sampled),
                ),
                style = roboto(12.sp, 16.sp),
                color = Neutral600,
            )
        }
    }
}

private const val TARGET_SHARE_OF_TRACK = 0.7

private fun loadFill(state: LoadState): Color = when (state) {
    LoadState.SEVERE_OVERLOAD -> Terracotta500
    LoadState.MODERATE_OVERLOAD -> Harvest300
    LoadState.BALANCED -> Green700
}

@Composable
private fun LoadStateChip(state: LoadState) {
    val (text, background, content) = when (state) {
        LoadState.SEVERE_OVERLOAD -> Triple(R.string.thinning_load_state_severe, Terracotta500, Neutral50)
        LoadState.MODERATE_OVERLOAD -> Triple(R.string.thinning_load_state_moderate, Harvest300, Green900)
        LoadState.BALANCED -> Triple(R.string.thinning_load_state_balanced, Green200, Green800)
    }
    Text(
        stringResource(text),
        style = robotoMedium(12.sp, 16.sp),
        color = content,
        modifier = Modifier.clip(CircleShape).background(background).padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/**
 * A 48 dp bar: the label on the fill, the value at the end of the fill when it reaches it (in the fill's
 * text colour), otherwise at the end of the track.
 */
@Composable
private fun LoadBar(label: String, value: String, fraction: Float, fill: Color, onFill: Color) {
    val share = fraction.coerceIn(0.12f, 1f)
    BoxWithConstraints(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(Neutral100)) {
        val fillWidth = maxWidth * share
        Box(Modifier.width(fillWidth).height(48.dp).clip(RoundedCornerShape(24.dp)).background(fill))
        Text(label, style = robotoMedium(13.sp, 18.sp), color = onFill, modifier = Modifier.align(Alignment.CenterStart).padding(start = 18.dp))
        val valueInsideFill = share >= VALUE_INSIDE_FILL
        Text(
            value,
            style = newsreader(20.sp, 24.sp),
            color = if (valueInsideFill) onFill else Neutral900,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = if (valueInsideFill) maxWidth - fillWidth + 18.dp else 29.dp),
        )
    }
}

private const val VALUE_INSIDE_FILL = 0.8f

@Composable
private fun YieldTiles(estimate: YieldEstimate?) {
    val dash = stringResource(R.string.thinning_no_value)
    val (first, second) = when {
        estimate == null || estimate.status == YieldEstimateStatus.LOAD_UNKNOWN || estimate.status == YieldEstimateStatus.AREA_UNKNOWN ->
            (dash to stringResource(R.string.thinning_yield_estimated)) to
                ((estimate?.sustainableKgPerHa?.let(::tons) ?: dash) to stringResource(R.string.thinning_yield_sustainable))
        estimate.status == YieldEstimateStatus.INSUFFICIENT_HISTORY ->
            (stringResource(R.string.thinning_yield_history_value, estimate.harvestCampaigns, estimate.requiredHarvestCampaigns) to
                stringResource(R.string.thinning_yield_history_caption)) to
                (dash to stringResource(R.string.thinning_yield_needs_history, estimate.requiredHarvestCampaigns))
        estimate.basis == YieldEstimateBasis.FRUIT_COUNT_ONLY ->
            (stringResource(R.string.thinning_yield_ceiling_value, estimate.estimatedKgPerHa?.let(::tons) ?: dash) to
                stringResource(R.string.thinning_yield_ceiling_caption)) to
                ((estimate.sustainableKgPerHa?.let(::tons) ?: dash) to stringResource(R.string.thinning_yield_sustainable))
        else ->
            ((estimate.estimatedKgPerHa?.let(::tons) ?: dash) to stringResource(R.string.thinning_yield_estimated)) to
                ((estimate.sustainableKgPerHa?.let(::tons) ?: dash) to stringResource(R.string.thinning_yield_sustainable))
    }
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        YieldTile(first.first, first.second, Modifier.weight(1f).fillMaxHeight())
        YieldTile(second.first, second.second, Modifier.weight(1f).fillMaxHeight())
    }
}

/** kg/ha from the backend as t/ha with one decimal (decision of 2026-10-04). */
internal fun tons(kgPerHa: Double): String = formatDecimal(kgPerHa / 1000.0, 1)

@Composable
private fun YieldTile(value: String, caption: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(Neutral100).padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(value, style = newsreader(24.sp, 28.sp).copy(letterSpacing = (-0.24).sp), color = Neutral900, maxLines = 1)
        Text(caption, style = roboto(12.sp, 16.sp), color = Neutral600, maxLines = 2)
    }
}

@Composable
private fun LoadReason(load: CurrentLoad, campaignYear: Int) {
    val excess = excessPercent(load.loadRatio)
    val look = when (load.loadState) {
        LoadState.SEVERE_OVERLOAD -> ReasonLook(
            Terracotta100, Terracotta700, Terracotta600, R.drawable.ic_warning,
            stringResource(R.string.thinning_reason_severe, excess, campaignYear + 1),
        )
        LoadState.MODERATE_OVERLOAD -> ReasonLook(
            ModerateReasonBackground, ModerateReasonContent, Terracotta600, R.drawable.ic_warning,
            stringResource(R.string.thinning_reason_moderate, excess, campaignYear + 1),
        )
        LoadState.BALANCED -> ReasonLook(Green200, Green800, Green800, R.drawable.ic_task_alt, stringResource(R.string.thinning_reason_balanced))
    }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(look.background).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(painterResource(look.icon), contentDescription = null, tint = look.iconTint, modifier = Modifier.size(20.dp))
        Text(look.text, style = roboto(13.sp, 18.sp), color = look.content, modifier = Modifier.weight(1f))
    }
}

private data class ReasonLook(val background: Color, val content: Color, val iconTint: Color, @DrawableRes val icon: Int, val text: String)

/** Figma "Carga moderada" reason box: a warm yellow between Harvest 100 and 300, and its brown text. */
private val ModerateReasonBackground = Color(0xFFFBEFC2)
private val ModerateReasonContent = Color(0xFF5C4A12)
private val ClosedWindowText = Color(0xFFE9A284)

@Composable
private fun PrescriptionCard(
    plan: ThinningPrescription,
    state: ThinningPlanUiState.Content,
    onRegister: () -> Unit,
    onViewResult: () -> Unit,
    onViewLoad: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val short = DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_short), locale)
    fun LocalDate.short(): String = short.format(this).replace(".", "")
    // Once recorded the window no longer matters: the card shows what was removed and leads to the result (P63).
    val executed = plan.status == PrescriptionStatus.EXECUTED
    val percentage = (if (executed) state.execution?.removalPercentage else null) ?: plan.percentageToRemove ?: 0.0
    val noThinning = !executed && (percentage <= 0.0 || plan.currentLoad?.loadState == LoadState.BALANCED)
    val phase = plan.windowPhase(state.today)
    val closed = phase is WindowPhase.Closed && !noThinning && !executed

    PrescriptionShell {
        val eyebrow = when {
            executed -> stringResource(R.string.thinning_executed_eyebrow)
            state.offline -> stringResource(R.string.thinning_saved_eyebrow)
            closed -> stringResource(R.string.thinning_closed_eyebrow, plan.windowClosesOn?.short().orEmpty().uppercase(locale))
            else -> stringResource(R.string.thinning_prescription_eyebrow)
        }
        Eyebrow(eyebrow)
        Figure(
            value = stringResource(R.string.thinning_percent, formatWhole(if (noThinning) 0 else percentage.roundToInt())),
            caption = stringResource(if (noThinning) R.string.thinning_no_thinning else R.string.thinning_of_fruits),
            dimmed = closed,
        )
        val instruction = when {
            executed -> stringResource(R.string.thinning_instruction_executed)
            noThinning -> stringResource(R.string.thinning_instruction_balanced)
            closed -> stringResource(R.string.thinning_instruction_closed)
            else -> removalFraction(percentage)?.let { (count, of) -> stringResource(R.string.thinning_instruction, count, of) }
                ?: stringResource(R.string.thinning_instruction_light)
        }
        Text(instruction, style = roboto(14.sp, 20.sp), color = Neutral50.copy(alpha = 0.8f), modifier = Modifier.padding(top = 12.dp, end = 10.dp))
        if (executed) {
            Spacer(Modifier.height(WINDOW_BLOCK_HEIGHT))
            ArrowPill(text = stringResource(R.string.thinning_view_result), dark = false, onClick = onViewResult)
        } else if (noThinning) {
            Spacer(Modifier.height(WINDOW_BLOCK_HEIGHT))
            ArrowPill(text = stringResource(R.string.thinning_view_load), dark = false, onClick = onViewLoad)
        } else {
            val opens = plan.windowOpensOn
            val closes = plan.windowClosesOn
            WindowBar(
                progress = when (phase) {
                    is WindowPhase.Open -> phase.progress
                    is WindowPhase.Closed -> 1f
                    else -> 0f
                },
                accent = if (closed) Terracotta500 else Harvest300,
                from = stringResource(R.string.thinning_window_from, opens?.short() ?: stringResource(R.string.thinning_no_value)),
                until = stringResource(R.string.thinning_window_until, closes?.short() ?: stringResource(R.string.thinning_no_value)),
            )
            val dayLine = when (phase) {
                is WindowPhase.Open -> if (phase.daysLeft == 0) {
                    stringResource(R.string.thinning_last_day)
                } else {
                    pluralStringResource(R.plurals.thinning_days_left, phase.daysLeft, phase.daysLeft)
                }
                is WindowPhase.NotOpenYet -> pluralStringResource(R.plurals.thinning_opens_in, phase.daysUntilOpen, phase.daysUntilOpen)
                is WindowPhase.Closed -> pluralStringResource(
                    R.plurals.thinning_closed_since,
                    phase.daysSinceClose,
                    phase.daysSinceClose,
                    DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_day_month), locale).format(state.today),
                )
                null -> ""
            }
            Text(dayLine, style = robotoMedium(13.sp, 18.sp), color = if (closed) ClosedWindowText else Harvest300, modifier = Modifier.padding(top = 10.dp))
            ArrowPill(
                text = stringResource(if (closed) R.string.thinning_register_anyway else R.string.thinning_register),
                dark = false,
                onClick = onRegister,
                modifier = Modifier.padding(top = 18.dp),
            )
        }
    }
}

/** Window bar, its labels and its 10 dp gap above the day line: 43 dp in Figma, replaced by space when there is no window. */
private val WINDOW_BLOCK_HEIGHT = 71.dp

/** The prescription card dimmed (35 %) while an input is missing: its shape without numbers that do not exist yet. */
@Composable
private fun GhostPrescriptionCard() {
    val dash = stringResource(R.string.thinning_no_value)
    PrescriptionShell(Modifier.alpha(0.35f)) {
        Eyebrow(stringResource(R.string.thinning_prescription_eyebrow))
        Figure(value = stringResource(R.string.thinning_percent, dash), caption = stringResource(R.string.thinning_of_fruits), dimmed = false)
        Text(
            stringResource(R.string.thinning_instruction_light),
            style = roboto(14.sp, 20.sp),
            color = Neutral50.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 12.dp, end = 10.dp),
        )
        WindowBar(
            progress = 0f,
            accent = Harvest300,
            from = stringResource(R.string.thinning_window_from, dash),
            until = stringResource(R.string.thinning_window_until, dash),
        )
        Spacer(Modifier.height(28.dp))
        ArrowPill(text = stringResource(R.string.thinning_register), dark = false, onClick = null, modifier = Modifier.padding(top = 18.dp))
    }
}

@Composable
private fun PrescriptionShell(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Green900)) {
        Image(
            painter = painterResource(R.drawable.ill_phase_blob),
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = 76.dp, y = (-90).dp).requiredSize(240.dp, 210.dp),
        )
        Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 22.dp)) { content() }
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(text, style = robotoMedium(11.sp, 16.sp).copy(letterSpacing = 1.54.sp), color = Neutral50.copy(alpha = 0.78f))
}

/** "30 % de los frutos" at 88 sp; on a phone narrower than the 305 dp it takes in Figma both sizes shrink together. */
@Composable
private fun Figure(value: String, caption: String, dimmed: Boolean) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val scale = (maxWidth / FIGURE_WIDTH).coerceAtMost(1f)
        FigureRow(value, caption, dimmed, scale)
    }
}

private val FIGURE_WIDTH = 305.dp

@Composable
private fun FigureRow(value: String, caption: String, dimmed: Boolean, scale: Float) {
    Row(
        modifier = Modifier.padding(top = 2.dp).offset(x = (-4).dp).alpha(if (dimmed) 0.45f else 1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            value,
            style = newsreader(88.sp * scale, 90.sp * scale).copy(letterSpacing = (-2.64).sp * scale),
            color = Neutral50,
            maxLines = 1,
            modifier = Modifier.alignByBaseline(),
        )
        Text(
            caption,
            style = newsreader(24.sp * scale, 30.sp * scale).copy(fontStyle = FontStyle.Italic),
            color = Neutral50,
            maxLines = 1,
            modifier = Modifier.alignByBaseline(),
        )
    }
}

@Composable
private fun WindowBar(progress: Float, accent: Color, from: String, until: String) {
    Column(Modifier.fillMaxWidth().padding(top = 11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(18.dp)) {
            val share = progress.coerceIn(0f, 1f)
            Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Neutral50.copy(alpha = 0.14f)))
            if (share > 0f) {
                Box(Modifier.align(Alignment.CenterStart).width(maxWidth * share).height(12.dp).clip(RoundedCornerShape(6.dp)).background(accent))
            }
            Box(
                Modifier.offset(x = ((maxWidth - 18.dp) * share)).size(18.dp).clip(CircleShape)
                    .background(Neutral50).border(3.dp, accent, CircleShape),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(from, style = roboto(11.sp, 14.sp), color = Neutral50.copy(alpha = 0.6f))
            Text(until, style = roboto(11.sp, 14.sp), color = Neutral50.copy(alpha = 0.6f))
        }
    }
}

/**
 * The compact pill of the plan cards: text and a 36 dp yellow circle with an arrow. Dark on the yellow cards,
 * light on the green prescription card; without [onClick] it is only drawn (the dimmed card).
 */
@Composable
private fun ArrowPill(text: String, dark: Boolean, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (dark) Green900 else Neutral50)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = robotoMedium(14.sp, 20.sp), color = if (dark) Neutral50 else Green900)
        Box(Modifier.size(36.dp).clip(CircleShape).background(Harvest300), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral900, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun MessageBody(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = Neutral700, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        PrimaryPillButton(text = stringResource(R.string.common_retry), onClick = onRetry)
    }
}
