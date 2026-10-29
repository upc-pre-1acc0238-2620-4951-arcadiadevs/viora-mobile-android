package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlin.math.roundToInt
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.component.VioraLoadingState
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.newsreader
import pe.edu.upc.viora.core.designsystem.theme.roboto
import pe.edu.upc.viora.core.designsystem.theme.robotoMedium
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.core.presentation.formatWhole
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.presentation.state.CampaignPlanUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.state.PlanRow
import pe.edu.upc.viora.features.croploadregulation.presentation.state.PlanRowStatus
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.CampaignPlanViewModel
import pe.edu.upc.viora.core.designsystem.component.VioraGreetingHeader
import pe.edu.upc.viora.core.designsystem.component.PlotSilhouette

/**
 * P60 · Plan, the Plan tab: the plots that need thinning this week in one block, then every plot with the state of
 * its plan. A row opens the plot's plan (P61) for the chosen campaign.
 */
@Composable
fun CampaignPlanScreen(
    onOpenPlan: (plotId: String, plotName: String, campaignYear: Int) -> Unit,
    onOpenAlerts: () -> Unit,
    onRegisterPlot: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CampaignPlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val open: (PlanRow) -> Unit = { onOpenPlan(it.plot.id.value, it.plot.name, state.campaignYear) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp),
    ) {
        VioraGreetingHeader(
            date = state.today,
            isOffline = state.isOffline,
            lastRefresh = state.lastRefresh,
            onOpenAlerts = onOpenAlerts,
            hasUnreadAlerts = state.hasUnreadAlerts,
        )
        Headline(state, onSelectCampaign = viewModel::selectCampaign, modifier = Modifier.padding(top = 28.dp))
        when {
            state.isLoading -> VioraLoadingState(modifier = Modifier.fillMaxWidth().padding(top = 80.dp))
            !state.hasPlots && state.error != null -> Message(
                text = stringResource(checkNotNull(state.error).messageRes()),
                action = stringResource(R.string.common_retry),
                onAction = viewModel::refresh,
            )
            !state.hasPlots -> Message(
                text = stringResource(R.string.plan_no_plots),
                action = stringResource(R.string.plan_register_plot),
                onAction = onRegisterPlot,
            )
            else -> {
                // "Esta semana" only exists in the running campaign; a past one goes straight to its plots.
                if (state.isCurrentCampaign) WeekCard(state, onOpen = open, modifier = Modifier.padding(top = 22.dp))
                Text(
                    stringResource(R.string.plan_your_plots),
                    style = newsreader(24.sp, 30.sp).copy(letterSpacing = (-0.24).sp),
                    color = Neutral900,
                    modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.rows.forEach { row -> PlanPlotRow(row, state, onClick = { open(row) }) }
                }
            }
        }
        Spacer(Modifier.height(VioraTabBarDefaults.ContentBottomPadding))
    }
}

/** "Tu *plan.*" with the campaign chip on its right, at the height of the second line (Figma P60). */
@Composable
private fun Headline(state: CampaignPlanUiState, onSelectCampaign: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            val style = newsreader(44.sp, 46.sp).copy(letterSpacing = (-1.1).sp)
            Text(stringResource(R.string.plan_headline_lead), style = style, color = Neutral900)
            Text(stringResource(R.string.plan_headline_emphasis), style = style.copy(fontStyle = FontStyle.Italic), color = Neutral900)
        }
        CampaignChip(state.campaignYear, state.campaignYears, onSelectCampaign, Modifier.padding(bottom = 4.dp))
    }
}

@Composable
private fun CampaignChip(year: Int, years: List<Int>, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier.clip(CircleShape).background(Green900).clickable(role = Role.Button) { open = true }
                .padding(start = 14.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.home_campaign, year), style = robotoMedium(13.sp, 18.sp), color = Neutral50)
            Icon(painterResource(R.drawable.ic_arrow_drop_down), contentDescription = null, tint = Neutral50, modifier = Modifier.size(24.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = Neutral0) {
            years.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_campaign, option), style = robotoMedium(14.sp, 20.sp), color = Neutral900) },
                    onClick = {
                        open = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

/**
 * "1 lote necesita aclareo esta semana" (terracotta): the plots whose window is open, the one closest to the pit
 * first. With none, the same block calms down in green and says when the next window opens.
 */
@Composable
private fun WeekCard(state: CampaignPlanUiState, onOpen: (PlanRow) -> Unit, modifier: Modifier = Modifier) {
    val urgent = state.urgent.sortedBy { it.daysLeft ?: Int.MAX_VALUE }
    val first = urgent.firstOrNull()
    val calm = first == null
    val accent = if (calm) Green800 else Terracotta700
    val locale = LocalConfiguration.current.locales[0]
    val subtitle = when {
        first != null -> {
            val days = first.daysLeft ?: 0
            val remaining = if (days == 0) stringResource(R.string.plan_week_last_day) else pluralStringResource(R.plurals.plan_week_days_left, days, days)
            if (urgent.size > 1) stringResource(R.string.plan_week_first, first.plot.name, remaining) else stringResource(R.string.plan_week_one, first.plot.name, remaining)
        }
        else -> state.nextOpening?.let { next ->
            val opens = next.prescription?.windowOpensOn ?: return@let null
            stringResource(R.string.plan_week_next_opening, next.plot.name, DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_day_month), locale).format(opens))
        } ?: stringResource(R.string.plan_week_nothing)
    }
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(if (calm) Green200 else Terracotta100)
            .then(if (first != null) Modifier.clickable(role = Role.Button) { onOpen(first) } else Modifier)
            .padding(start = 22.dp, end = 18.dp, top = 14.dp, bottom = 22.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                formatWhole(urgent.size),
                style = newsreader(72.sp, 72.sp).copy(letterSpacing = (-2.16).sp),
                color = accent,
            )
            Text(
                pluralStringResource(R.plurals.plan_week_title, urgent.size),
                style = roboto(22.sp, 26.sp).copy(letterSpacing = (-0.22).sp),
                color = accent,
                // Two lines as in Figma: narrower phones shrink the words instead of adding a third line.
                maxLines = 2,
                autoSize = TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 22.sp),
                modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp, top = 14.dp),
            )
            if (first != null) {
                Box(Modifier.padding(top = 4.dp).size(40.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Terracotta700, modifier = Modifier.size(24.dp))
                }
            }
        }
        Text(subtitle, style = roboto(13.sp, 18.sp), color = Neutral700, modifier = Modifier.padding(top = 12.dp))
    }
}

/** One plot: silhouette, name, what its plan says in one line and the pill of its state (Figma "Lote · …"). */
@Composable
internal fun PlanPlotRow(row: PlanRow, state: CampaignPlanUiState, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlotSilhouette(outline = row.plot.outline, modifier = Modifier.size(64.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                row.plot.name,
                style = newsreader(22.sp, 26.sp).copy(letterSpacing = (-0.22).sp),
                color = Neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(rowDetail(row, state), style = roboto(12.sp, 16.sp), color = Neutral600, maxLines = 1, overflow = TextOverflow.Ellipsis)
            StatusPill(row.status)
        }
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Neutral600)
    }
}

private data class PillLook(val background: Color, val dot: Color, val content: Color, val label: Int)

private fun pillLook(status: PlanRowStatus): PillLook = when (status) {
    PlanRowStatus.RECOMMENDED -> PillLook(Terracotta100, Terracotta500, Terracotta700, R.string.plan_status_recommended)
    PlanRowStatus.WINDOW_NOT_OPEN -> PillLook(Harvest100, Harvest400, Harvest800, R.string.plan_status_not_open)
    PlanRowStatus.NO_BLOOM -> PillLook(Harvest100, Harvest400, Harvest800, R.string.plan_status_no_bloom)
    PlanRowStatus.INCOMPLETE_SAMPLE -> PillLook(Harvest100, Harvest400, Harvest800, R.string.plan_status_incomplete)
    PlanRowStatus.NO_PROFILE -> PillLook(Neutral100, Neutral300, Neutral700, R.string.plan_status_no_profile)
    PlanRowStatus.NO_SAMPLING -> PillLook(Neutral100, Neutral300, Neutral700, R.string.plan_status_no_sampling)
    PlanRowStatus.WINDOW_CLOSED -> PillLook(Neutral100, Terracotta500, Terracotta700, R.string.plan_status_closed)
    PlanRowStatus.EXECUTED -> PillLook(Green200, Green800, Green800, R.string.plan_status_executed)
    PlanRowStatus.BALANCED -> PillLook(Green200, Green800, Green800, R.string.plan_status_balanced)
}

@Composable
private fun StatusPill(status: PlanRowStatus) {
    val look = pillLook(status)
    Row(
        modifier = Modifier.clip(CircleShape).background(look.background).padding(start = 10.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(look.dot))
        Text(stringResource(look.label), style = robotoMedium(12.sp, 16.sp), color = look.content, maxLines = 1)
    }
}

/** The line under the name: the figure that matters for each state ("Carga crítica · 30 % a remover"). */
@Composable
private fun rowDetail(row: PlanRow, state: CampaignPlanUiState): String {
    val locale = LocalConfiguration.current.locales[0]
    val short = DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_short), locale)
    fun LocalDate.short(): String = short.format(this).replace(".", "")
    val plan = row.prescription
    val percent = plan?.percentageToRemove?.let { stringResource(R.string.thinning_percent, formatWhole(it.roundToInt())) }.orEmpty()
    return when (row.status) {
        PlanRowStatus.RECOMMENDED -> stringResource(R.string.plan_detail_recommended, loadLabel(plan?.currentLoad?.loadState), percent)
        PlanRowStatus.WINDOW_NOT_OPEN ->
            stringResource(R.string.plan_detail_not_open, plan?.windowOpensOn?.short().orEmpty(), percent)
        PlanRowStatus.WINDOW_CLOSED -> stringResource(R.string.plan_detail_closed, plan?.windowClosesOn?.short().orEmpty())
        PlanRowStatus.EXECUTED -> row.execution?.let {
            stringResource(
                R.string.plan_detail_executed,
                stringResource(R.string.thinning_percent, formatWhole(it.removalPercentage.roundToInt())),
                it.executedOn.short(),
            )
        } ?: stringResource(R.string.plan_detail_executed_no_detail)
        PlanRowStatus.BALANCED -> stringResource(R.string.plan_detail_balanced)
        PlanRowStatus.INCOMPLETE_SAMPLE -> stringResource(
            R.string.plan_detail_incomplete,
            plan?.sampledTrees ?: 0,
            ThinningPrescription.SAMPLE_SIZE,
        )
        PlanRowStatus.NO_BLOOM -> stringResource(R.string.plan_detail_no_bloom)
        PlanRowStatus.NO_PROFILE -> stringResource(R.string.plan_detail_no_profile)
        PlanRowStatus.NO_SAMPLING -> row.plot.registeredAt?.takeIf { state.isCurrentCampaign }?.let {
            val day = it.atZone(LocalAppZone.current).toLocalDate()
            stringResource(R.string.plan_detail_registered, DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_day_month), locale).format(day))
        } ?: stringResource(R.string.plan_detail_no_sampling, state.campaignYear)
    }
}

@Composable
private fun loadLabel(state: LoadState?): String = stringResource(
    when (state) {
        LoadState.SEVERE_OVERLOAD -> R.string.plan_load_severe
        LoadState.MODERATE_OVERLOAD -> R.string.plan_load_moderate
        LoadState.BALANCED, null -> R.string.plan_load_heavy
    },
)

@Composable
private fun Message(text: String, action: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Neutral700, textAlign = TextAlign.Center)
        PrimaryPillButton(text = action, onClick = onAction)
    }
}
