package pe.edu.upc.viora.features.subscription.presentation.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.component.PhaseCardArtwork
import pe.edu.upc.viora.core.designsystem.component.PhaseIllustration
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.core.presentation.vioraNumberFormat
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton
import pe.edu.upc.viora.features.subscription.presentation.state.MyPlanUiState
import pe.edu.upc.viora.features.subscription.presentation.state.PlanPlot
import pe.edu.upc.viora.features.subscription.presentation.state.PlanStanding
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.MyPlanViewModel

/** Where the actions of P99 lead; the composition root knows the routes. */
class MyPlanActions(
    val onBack: () -> Unit,
    val onRegisterPlot: () -> Unit,
    val onOpenPlans: () -> Unit,
    val onRedeemCode: () -> Unit,
)

/**
 * P99 · Mi plan (Figma section 21): the hectare ring (one arc per plot, the free hectares in the middle), the
 * validity, the plots inside the plan and its details. On entry the ring draws itself from twelve o'clock plot by
 * plot while the figure counts up, and the cards below rise in turn; with the system animations off it opens
 * already drawn.
 */
@Composable
fun MyPlanScreen(actions: MyPlanActions, modifier: Modifier = Modifier, viewModel: MyPlanViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize().background(Neutral100).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(icon = R.drawable.ic_arrow_back, contentDescription = stringResource(R.string.action_back), onClick = actions.onBack)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.my_plan_title), style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp), color = Neutral900)
                Text(stringResource(R.string.account_role_producer), style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = Neutral600)
            }
            Spacer(Modifier.size(48.dp))
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green900)
            }
            state.error != null -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(state.error!!.messageRes()), textAlign = TextAlign.Center, color = Neutral900)
                Spacer(Modifier.height(16.dp))
                PrimaryPillButton(text = stringResource(R.string.my_plan_retry), onClick = viewModel::refresh)
            }
            else -> MyPlanContent(state, actions)
        }
    }
}

@Composable
private fun MyPlanContent(state: MyPlanUiState, actions: MyPlanActions) {
    val context = LocalContext.current
    val animationsOff = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    // One clock for the whole entrance: 0 → 1 over 900 ms; each block reads its own window of it.
    val entrance = remember { Animatable(if (animationsOff) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!animationsOff) launch { entrance.animateTo(1f, tween(durationMillis = ENTRANCE_MILLIS, easing = FastOutSlowInEasing)) }
    }
    val t = entrance.value
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        EditorialHeadline(lead = stringResource(R.string.my_plan_headline_lead), emphasis = stringResource(R.string.my_plan_headline_emphasis))
        PlanHero(state, progress = t, onAction = {
            when (state.standing) {
                PlanStanding.ACTIVE -> if (state.isOverQuota) actions.onOpenPlans() else actions.onRegisterPlot()
                PlanStanding.EXPIRING, PlanStanding.NONE -> actions.onOpenPlans()
            }
        })
        val rise = { index: Int -> Modifier.rise(t, delay = 0.35f + index * 0.08f) }
        if (state.standing != PlanStanding.NONE) {
            PlanSection(stringResource(R.string.my_plan_section_validity), rise(0)) { ValidityCard(state) }
        }
        if (state.plots.isNotEmpty()) {
            PlanSection(stringResource(R.string.my_plan_section_plots), rise(1)) { PlotsCard(state, t) }
        }
        VioraVoice(text = voiceLine(state), modifier = rise(2))
        if (state.standing != PlanStanding.NONE) {
            PlanSection(stringResource(R.string.my_plan_section_details), rise(3)) { DetailsCard(state) }
        }
        Column(rise(4), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                state.standing == PlanStanding.NONE -> OutlinedPillButton(stringResource(R.string.my_plan_have_code), actions.onRedeemCode)
                state.activatedWithCode -> OutlinedPillButton(stringResource(R.string.my_plan_other_code), actions.onRedeemCode)
                state.standing == PlanStanding.EXPIRING -> OutlinedPillButton(stringResource(R.string.my_plan_renew_and_expand), actions.onOpenPlans)
                // Over the quota the hero already says "Ampliar mi plan".
                !state.isOverQuota -> OutlinedPillButton(stringResource(R.string.my_plan_expand), actions.onOpenPlans)
            }
            Text(
                stringResource(R.string.my_plan_how_plans_work),
                style = MaterialTheme.typography.titleMedium,
                color = Neutral900,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = actions.onOpenPlans)
                    .padding(vertical = 16.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** A block of the page enters 24 dp below and transparent, and settles within its share of the entrance. */
private fun Modifier.rise(t: Float, delay: Float): Modifier = graphicsLayer {
    val local = ((t - delay) / (1f - delay)).coerceIn(0f, 1f)
    alpha = local
    translationY = (1f - local) * 24.dp.toPx()
}

@Composable
private fun PlanHero(state: MyPlanUiState, progress: Float, onAction: () -> Unit) {
    val onDark = Neutral50.copy(alpha = 0.78f)
    val none = state.standing == PlanStanding.NONE
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Green900)) {
        PhaseCardArtwork(PhaseIllustration.THINNING)
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(if (state.standing == PlanStanding.ACTIVE && !state.isOverQuota) Harvest300 else Terracotta500))
                Text(eyebrow(state).uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.54.sp), color = onDark)
            }
            Spacer(Modifier.height(14.dp))
            HectareRing(state, progress, Modifier.align(Alignment.CenterHorizontally))
            if (!none && state.plots.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).graphicsLayer { alpha = ((progress - 0.5f) * 2f).coerceIn(0f, 1f) },
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    state.plots.forEachIndexed { index, plot -> LegendChip("${plot.name} · ${hectares(plot.hectares)}", plotColor(index)) }
                    if (!state.isOverQuota) {
                        LegendChip(stringResource(R.string.my_plan_free_chip, hectares(state.freeHectares)), Neutral50.copy(alpha = 0.35f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(heroLine(state), style = MaterialTheme.typography.bodyMedium, color = onDark)
            Spacer(Modifier.height(18.dp))
            HeroPill(
                label = stringResource(
                    when (state.standing) {
                        PlanStanding.ACTIVE -> if (state.isOverQuota) R.string.my_plan_expand else R.string.my_plan_register_plot
                        PlanStanding.EXPIRING -> R.string.my_plan_renew
                        PlanStanding.NONE -> R.string.my_plan_activate
                    },
                ),
                onClick = onAction,
            )
        }
    }
}

/**
 * The quota as a ring: each plot an arc in its color, drawn in turn from twelve o'clock as [progress] grows; the
 * free hectares count up in the middle. Without a plan the ring stays empty and counts the hectares kept.
 */
@Composable
private fun HectareRing(state: MyPlanUiState, progress: Float, modifier: Modifier = Modifier) {
    val none = state.standing == PlanStanding.NONE
    // Over the quota the plots share the whole circle; the Terracotta eyebrow dot tells the excess.
    val quota = (if (state.isOverQuota) state.usedHectares else state.quotaHectares).takeIf { it > 0 } ?: 1.0
    Box(modifier.size(232.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(232.dp)) {
            val stroke = 16.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(Neutral50.copy(alpha = 0.12f), -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            if (none) return@Canvas
            // The plots are drawn one after the other up to [progress] of the hectares used.
            val drawnUpTo = state.usedHectares * progress
            var start = 0.0
            state.plots.forEachIndexed { index, plot ->
                val end = start + plot.hectares
                val visibleEnd = minOf(end, drawnUpTo)
                if (visibleEnd > start) {
                    val gap = if (state.plots.size > 1) 1.2f else 0f
                    val sweep = ((visibleEnd - start) / quota * 360f).toFloat() - gap
                    if (sweep > 0f) {
                        drawArc(plotColor(index), (-90f + start / quota * 360f).toFloat() + gap / 2, sweep, false, topLeft, arcSize,
                            style = Stroke(stroke, cap = StrokeCap.Butt))
                    }
                }
                start = end
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val shown = when {
                none -> state.usedHectares
                state.isOverQuota -> state.excessHectares
                else -> state.freeHectares
            } * progress
            Text(
                vioraNumberFormat(1, 1).format(shown),
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = NewsreaderFamily, fontSize = 72.sp, lineHeight = 72.sp),
                color = Neutral50,
            )
            Text(
                stringResource(
                    when {
                        none -> R.string.my_plan_ring_kept
                        state.isOverQuota -> R.string.my_plan_ring_over
                        else -> R.string.my_plan_ring_free
                    },
                ),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NewsreaderFamily, fontSize = 22.sp),
                fontStyle = FontStyle.Italic,
                color = Neutral50,
            )
            Text(
                if (none) pluralStringResource(R.plurals.my_plan_ring_plots, state.plots.size, state.plots.size)
                else stringResource(R.string.my_plan_ring_of, hectares(state.quotaHectares)),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Neutral50.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(
        Modifier.clip(CircleShape).background(Neutral50.copy(alpha = 0.1f)).padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = Neutral50, maxLines = 1)
    }
}

@Composable
private fun HeroPill(label: String, onClick: () -> Unit) {
    Row(
        Modifier
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

@Composable
private fun PlanSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.88.sp),
            color = Neutral600,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        content()
    }
}

@Composable
private fun ValidityCard(state: MyPlanUiState) {
    val expiring = state.standing == PlanStanding.EXPIRING
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(if (expiring) Terracotta100 else Neutral0).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                state.daysLeft.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontFamily = NewsreaderFamily, fontSize = 44.sp, lineHeight = 44.sp),
                color = Neutral900,
            )
            Text(
                pluralStringResource(if (expiring) R.plurals.my_plan_days_to_renew else R.plurals.my_plan_days_left, state.daysLeft),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = NewsreaderFamily, fontSize = 20.sp),
                fontStyle = FontStyle.Italic,
                color = Neutral900,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Bar(fraction = state.elapsedShare, color = if (expiring) Terracotta500 else Green900)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            state.startsOn?.let { Text(stringResource(R.string.my_plan_since, date(it)), style = MaterialTheme.typography.bodySmall, color = Neutral600) }
            state.endsOn?.let {
                Text(
                    stringResource(if (expiring) R.string.my_plan_ends_on else R.string.my_plan_until, date(it)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                )
            }
        }
    }
}

@Composable
private fun PlotsCard(state: MyPlanUiState, progress: Float) {
    val none = state.standing == PlanStanding.NONE
    val scale = (if (none || state.isOverQuota) state.usedHectares else state.quotaHectares).coerceAtLeast(0.01)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.plots.forEachIndexed { index, plot ->
            PlotLine(
                name = plot.name,
                detail = plotDetail(plot),
                hectares = plot.hectares,
                color = plotColor(index),
                fraction = (plot.hectares / scale).toFloat() * progress,
                dimmed = none,
            )
        }
        if (!none && !state.isOverQuota) {
            PlotLine(
                name = stringResource(R.string.my_plan_free_label),
                detail = pluralStringResource(R.plurals.my_plan_free_detail, state.plotsThatFit, state.plotsThatFit),
                hectares = state.freeHectares,
                color = Neutral200,
                fraction = 0f,
                dimmed = false,
            )
        }
    }
}

@Composable
private fun PlotLine(name: String, detail: String, hectares: Double, color: Color, fraction: Float, dimmed: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp), color = Neutral900, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = Neutral600, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                stringResource(R.string.my_plan_hectares, hectares(hectares)),
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = NewsreaderFamily, fontSize = 22.sp),
                color = Neutral900,
            )
        }
        Bar(fraction = fraction, color = color, alpha = if (dimmed) 0.45f else 1f)
    }
}

@Composable
private fun Bar(fraction: Float, color: Color, alpha: Float = 1f) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Neutral200)) {
        val width = maxWidth * fraction.coerceIn(0f, 1f)
        if (fraction > 0f) Box(Modifier.width(width).height(8.dp).clip(RoundedCornerShape(4.dp)).background(color.copy(alpha = color.alpha * alpha)))
    }
}

@Composable
private fun DetailsCard(state: MyPlanUiState) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(horizontal = 20.dp, vertical = 4.dp)) {
        DetailRow(stringResource(R.string.my_plan_detail_plan), stringResource(R.string.my_plan_detail_plan_value, hectares(state.quotaHectares)), divider = true)
        DetailRow(
            stringResource(R.string.my_plan_detail_activation),
            stringResource(if (state.activatedWithCode) R.string.my_plan_detail_by_code else R.string.my_plan_detail_paid),
            divider = true,
        )
        DetailRow(
            stringResource(R.string.my_plan_detail_receipt),
            stringResource(if (state.activatedWithCode) R.string.my_plan_detail_no_receipt else R.string.my_plan_detail_receipt_sent),
            divider = false,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, divider: Boolean) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 15.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp), color = Neutral900)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp), color = Neutral600)
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(Neutral900.copy(alpha = 0.08f)))
    }
}

/**
 * Mi cuenta · "TU PLAN" (P95 · Con plan): the free hectares, the quota bar and the validity on a dark card; it opens
 * P99. While the server has not answered it draws nothing.
 */
@Composable
fun AccountPlanCard(onOpen: () -> Unit, modifier: Modifier = Modifier, viewModel: MyPlanViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.isLoading || state.error != null) return
    val onDark = Neutral50.copy(alpha = 0.78f)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            stringResource(R.string.my_plan_account_section).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.88.sp),
            color = Neutral600,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Green900)
                .clickable(role = Role.Button, onClick = onOpen)
                .padding(start = 22.dp, end = 18.dp, top = 20.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(if (state.standing == PlanStanding.ACTIVE && !state.isOverQuota) Harvest300 else Terracotta500))
                    Text(eyebrow(state).uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.54.sp), color = onDark, maxLines = 1)
                }
                if (state.standing == PlanStanding.NONE) {
                    Text(
                        stringResource(R.string.my_plan_account_none),
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NewsreaderFamily, fontSize = 26.sp),
                        color = Neutral50,
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                        Text(
                            vioraNumberFormat(1, 1).format(if (state.isOverQuota) state.excessHectares else state.freeHectares),
                            style = MaterialTheme.typography.displaySmall.copy(fontFamily = NewsreaderFamily, fontSize = 52.sp, lineHeight = 52.sp),
                            color = Neutral50,
                        )
                        Text(
                            stringResource(if (state.isOverQuota) R.string.my_plan_ring_over else R.string.my_plan_ring_free),
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = NewsreaderFamily, fontSize = 22.sp),
                            fontStyle = FontStyle.Italic,
                            color = Neutral50,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    BoxWithConstraints(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Neutral50.copy(alpha = 0.14f))) {
                        val used = (state.usedHectares / state.quotaHectares.coerceAtLeast(0.01)).toFloat().coerceIn(0f, 1f)
                        Box(Modifier.width(maxWidth * used).height(8.dp).clip(RoundedCornerShape(4.dp)).background(if (state.isOverQuota) Terracotta500 else Harvest300))
                    }
                    Text(
                        stringResource(
                            R.string.my_plan_account_caption,
                            hectares(state.usedHectares),
                            hectares(state.quotaHectares),
                            state.endsOn?.let { date(it) }.orEmpty(),
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Neutral50.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(Modifier.padding(start = 12.dp).size(36.dp).clip(CircleShape).background(Harvest300), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Neutral900, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private const val ENTRANCE_MILLIS = 900

/** The color of each plot's arc, chip and bar, in order. */
private val PlotPalette = listOf(Harvest300, Harvest100, Green200, Terracotta100)

private fun plotColor(index: Int): Color = PlotPalette[index % PlotPalette.size]

@Composable
private fun hectares(value: Double): String = vioraNumberFormat(0, 1).format(value)

@Composable
private fun date(value: LocalDate): String =
    DateTimeFormatter.ofPattern(stringResource(R.string.my_plan_date_pattern), LocalConfiguration.current.locales[0]).format(value)

@Composable
private fun plotDetail(plot: PlanPlot): String =
    stringResource(R.string.my_plan_plot_detail, stringResource(plot.variety.labelRes()), vioraNumberFormat(0, 0).format(plot.trees))

@Composable
private fun eyebrow(state: MyPlanUiState): String = when (state.standing) {
    PlanStanding.NONE -> stringResource(R.string.my_plan_eyebrow_none)
    PlanStanding.EXPIRING -> pluralStringResource(R.plurals.my_plan_eyebrow_expiring, state.daysLeft, state.daysLeft)
    PlanStanding.ACTIVE -> stringResource(if (state.activatedWithCode) R.string.my_plan_eyebrow_code else R.string.my_plan_eyebrow_active)
}

@Composable
private fun heroLine(state: MyPlanUiState): String = when (state.standing) {
    PlanStanding.NONE -> stringResource(R.string.my_plan_hero_none)
    PlanStanding.EXPIRING -> stringResource(R.string.my_plan_hero_expiring, state.endsOn?.let { date(it) }.orEmpty())
    PlanStanding.ACTIVE -> if (state.isOverQuota) {
        stringResource(R.string.my_plan_hero_over, hectares(state.usedHectares), hectares(state.quotaHectares))
    } else stringResource(
        if (state.activatedWithCode) R.string.my_plan_hero_code else R.string.my_plan_hero_active,
        hectares(state.quotaHectares),
        state.endsOn?.let { date(it) }.orEmpty(),
        hectares(state.usedHectares),
        state.plots.size,
    )
}

@Composable
private fun voiceLine(state: MyPlanUiState): String = when {
    state.standing == PlanStanding.NONE -> stringResource(R.string.my_plan_voice_none)
    state.standing == PlanStanding.EXPIRING -> stringResource(R.string.my_plan_voice_expiring)
    state.isOverQuota -> stringResource(R.string.my_plan_voice_over, hectares(state.excessHectares))
    state.activatedWithCode -> stringResource(R.string.my_plan_voice_code)
    else -> stringResource(R.string.my_plan_voice_active, hectares(state.freeHectares))
}
