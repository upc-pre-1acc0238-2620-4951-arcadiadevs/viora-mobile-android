package pe.edu.upc.viora.features.telemetry.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Duration
import java.time.Instant
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.telemetry.domain.entity.SoilMoistureRules
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SoilMoistureStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange
import pe.edu.upc.viora.features.telemetry.presentation.state.TelemetryDetailUiState
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.AxisLabel
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ChartAxis
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ChartPoint
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ClimateErrorCard
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ClimateHeadline
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ClimateNotice
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ClimateTopBar
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.MetricLineChart
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.SelectablePill
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatDayMonth
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatDegrees
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatElapsed
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatPercent
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatReadingStamp
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.fullWeekday
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.shortWeekday
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.timeAxisLabels
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.windowFraction
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.TelemetryDetailViewModel

private val ScreenPadding = 24.dp
private val CardShape = RoundedCornerShape(28.dp)
private const val SOIL_PROBE_DEPTH_CM = 30
private val AFTER_IRRIGATION_WINDOW: Duration = Duration.ofHours(6)

/**
 * The curve of one metric (Figma P91 "Humedad del suelo"): the last reading with its timestamp,
 * the series over 24 hours, 7 or 30 days, and the minimum, average and maximum (US17, scenario 1).
 */
@Composable
fun TelemetryDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TelemetryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TelemetryDetailContent(
        state = state,
        onBack = onBack,
        onSelectMetric = viewModel::selectMetric,
        onSelectRange = viewModel::selectRange,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelemetryDetailContent(
    state: TelemetryDetailUiState,
    onBack: () -> Unit,
    onSelectMetric: (TelemetryMetric) -> Unit,
    onSelectRange: (TelemetryRange) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state as? TelemetryDetailUiState.Content
    Column(modifier = modifier.fillMaxSize().background(Neutral100).statusBarsPadding()) {
        ClimateTopBar(
            title = content?.plotName.orEmpty(),
            subtitle = topSubtitle(state.metric),
            onBack = onBack,
            onMore = null,
        )
        PullToRefreshBox(
            isRefreshing = content?.isRefreshing == true,
            onRefresh = onRefresh,
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding),
            ) {
                Spacer(Modifier.height(16.dp))
                ClimateHeadline(
                    lead = stringResource(headlineLead(state.metric)),
                    emphasis = stringResource(headlineEmphasis(state.metric)),
                )
                Spacer(Modifier.height(16.dp))
                MetricChips(selected = state.metric, onSelect = onSelectMetric)
                Spacer(Modifier.height(20.dp))
                when (state) {
                    is TelemetryDetailUiState.Loading -> Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = Green900) }
                    is TelemetryDetailUiState.Error -> ClimateErrorCard(error = state.error, onRetry = onRefresh)
                    is TelemetryDetailUiState.Content -> DetailBody(state, onSelectRange)
                }
                Spacer(Modifier.height(VioraTabBarDefaults.ContentBottomPadding + 24.dp))
            }
        }
    }
}

@Composable
private fun topSubtitle(metric: TelemetryMetric): String = when (metric) {
    TelemetryMetric.SOIL_MOISTURE -> stringResource(R.string.metric_top_soil, SOIL_PROBE_DEPTH_CM)
    else -> stringResource(R.string.metric_top_microclimate)
}

private fun headlineLead(metric: TelemetryMetric): Int = when (metric) {
    TelemetryMetric.TEMPERATURE -> R.string.metric_title_temperature_lead
    TelemetryMetric.AIR_HUMIDITY -> R.string.metric_title_air_lead
    TelemetryMetric.SOIL_MOISTURE -> R.string.metric_title_soil_lead
}

private fun headlineEmphasis(metric: TelemetryMetric): Int = when (metric) {
    TelemetryMetric.TEMPERATURE -> R.string.metric_title_temperature_emphasis
    TelemetryMetric.AIR_HUMIDITY -> R.string.metric_title_air_emphasis
    TelemetryMetric.SOIL_MOISTURE -> R.string.metric_title_soil_emphasis
}

@Composable
private fun MetricChips(selected: TelemetryMetric, onSelect: (TelemetryMetric) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SelectablePill(
            text = stringResource(R.string.climate_temperature),
            selected = selected == TelemetryMetric.TEMPERATURE,
            onClick = { onSelect(TelemetryMetric.TEMPERATURE) },
        )
        SelectablePill(
            text = stringResource(R.string.climate_air_humidity),
            selected = selected == TelemetryMetric.AIR_HUMIDITY,
            onClick = { onSelect(TelemetryMetric.AIR_HUMIDITY) },
        )
        SelectablePill(
            text = stringResource(R.string.metric_chip_soil),
            selected = selected == TelemetryMetric.SOIL_MOISTURE,
            onClick = { onSelect(TelemetryMetric.SOIL_MOISTURE) },
        )
    }
}

/** Formats a value of [metric]: degrees with one decimal for the temperature, whole percent otherwise. */
private fun formatMetric(locale: Locale, metric: TelemetryMetric, value: Double): String =
    if (metric == TelemetryMetric.TEMPERATURE) formatDegrees(locale, value, 1) else formatPercent(locale, value)

@Composable
private fun DetailBody(state: TelemetryDetailUiState.Content, onSelectRange: (TelemetryRange) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val latest = state.latest

    if (state.isFromCache) {
        state.lastRefresh?.let {
            ClimateNotice(stringResource(R.string.metric_offline_notice, formatReadingStamp(it, state.zone, locale)))
            Spacer(Modifier.height(16.dp))
        }
    }

    LastReadingCard(state, locale)

    if (state.metric == TelemetryMetric.SOIL_MOISTURE && latest != null) {
        Spacer(Modifier.height(16.dp))
        SoilAdvice(state, locale)
    }

    Spacer(Modifier.height(20.dp))
    RangeSelector(selected = state.range, onSelect = onSelectRange)
    Spacer(Modifier.height(12.dp))

    Column(modifier = Modifier.fillMaxWidth().clip(CardShape).background(Neutral0).padding(vertical = 12.dp)) {
        if (state.points.isEmpty()) {
            Text(
                text = stringResource(R.string.metric_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            MetricChart(state, locale)
        }
    }

    state.stats?.let { stats ->
        Spacer(Modifier.height(12.dp))
        StatsRow(state, stats.min.value, stats.average, stats.max.value, locale)
    }
}

// ─── Last reading ───────────────────────────────────────────────────────────────────────────

@Composable
private fun LastReadingCard(state: TelemetryDetailUiState.Content, locale: Locale) {
    val latest = state.latest
    val status = state.soilStatus
    val container = when (status) {
        SoilMoistureStatus.STRESS -> Terracotta100
        SoilMoistureStatus.WATCH -> Harvest100
        else -> Green200
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(container)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = latest?.let { stringResource(R.string.metric_last_reading, formatElapsed(it.observedAt, state.now)) }
                    ?: stringResource(R.string.metric_no_reading),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = Neutral700,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            if (status != null) StatusPill(status)
        }
        Text(
            text = latest?.let { formatMetric(locale, state.metric, it.value) } ?: "—",
            style = TextStyle(fontFamily = NewsreaderFamily, fontSize = 72.sp, lineHeight = 76.sp),
            color = Neutral900,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (latest != null) {
            val stamp = formatReadingStamp(latest.observedAt, state.zone, locale)
            Text(
                text = if (state.metric == TelemetryMetric.SOIL_MOISTURE) {
                    stringResource(
                        R.string.metric_stamp_soil,
                        stamp,
                        formatPercent(locale, SoilMoistureRules.RECHARGE_POINT_PERCENT),
                    )
                } else {
                    stamp
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral700,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun StatusPill(status: SoilMoistureStatus) {
    val (label, dot) = when (status) {
        SoilMoistureStatus.IN_RANGE -> R.string.metric_status_in_range to Green900
        SoilMoistureStatus.WATCH -> R.string.metric_status_watch to Harvest800
        SoilMoistureStatus.STRESS -> R.string.metric_status_stress to Terracotta700
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Neutral0)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
        Text(text = stringResource(label), style = MaterialTheme.typography.labelMedium, color = Neutral900)
    }
}

/** The irrigation advice under the last reading ("Tu suelo está bien. A este ritmo, conviene regar el jueves..."). */
@Composable
private fun SoilAdvice(state: TelemetryDetailUiState.Content, locale: Locale) {
    val status = state.soilStatus ?: return
    val recharge = formatPercent(locale, SoilMoistureRules.RECHARGE_POINT_PERCENT)
    val watchLevel = formatPercent(locale, SoilMoistureRules.WATCH_LEVEL_PERCENT)
    val projected = state.projectedWatchLevelAt
    val text = when (status) {
        SoilMoistureStatus.STRESS -> stringResource(R.string.metric_hint_stress, recharge)
        SoilMoistureStatus.WATCH -> stringResource(R.string.metric_hint_watch, recharge)
        SoilMoistureStatus.IN_RANGE -> if (projected != null) {
            stringResource(
                R.string.metric_hint_ok_projection,
                fullWeekday(projected.atZone(state.zone).toLocalDate(), locale),
                watchLevel,
            )
        } else {
            stringResource(R.string.metric_hint_ok_stable)
        }
    }
    val icon = if (status == SoilMoistureStatus.IN_RANGE) R.drawable.ic_task_alt else R.drawable.ic_warning
    val tint = if (status == SoilMoistureStatus.IN_RANGE) Green900 else Terracotta700
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp).padding(top = 2.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = Neutral700,
        )
    }
}

// ─── Range and chart ────────────────────────────────────────────────────────────────────────

@Composable
private fun RangeSelector(selected: TelemetryRange, onSelect: (TelemetryRange) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(Neutral0)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TelemetryRange.entries.forEach { range ->
            val isSelected = range == selected
            Text(
                text = stringResource(rangeLabel(range)),
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Color.White else Neutral900,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (isSelected) Green900 else Color.Transparent)
                    .clickable(role = Role.Tab, onClick = { onSelect(range) })
                    .padding(vertical = 10.dp),
            )
        }
    }
}

private fun rangeLabel(range: TelemetryRange): Int = when (range) {
    TelemetryRange.LAST_24_HOURS -> R.string.metric_range_24h
    TelemetryRange.LAST_7_DAYS -> R.string.metric_range_7d
    TelemetryRange.LAST_30_DAYS -> R.string.metric_range_30d
}

@Composable
private fun MetricChart(state: TelemetryDetailUiState.Content, locale: Locale) {
    val metric = state.metric
    val isSoil = metric == TelemetryMetric.SOIL_MOISTURE
    val values = state.points.map { it.value }
    // The scale also contains the recharge point, so the stress band is always visible.
    val lo = if (isSoil) minOf(values.min(), SoilMoistureRules.RECHARGE_POINT_PERCENT) else values.min()
    val axis = ChartAxis.nice(lo, values.max())
    val latest = state.latest
    val chartPoints = state.points.map { ChartPoint(windowFraction(it.observedAt, state.range, state.now), it.value) }
    val labels: List<AxisLabel> = timeAxisLabels(state.range, state.now, state.zone, locale)
    val latestText = latest?.let { formatMetric(locale, metric, it.value) }.orEmpty()
    val rechargeText = formatPercent(locale, SoilMoistureRules.RECHARGE_POINT_PERCENT)

    MetricLineChart(
        points = chartPoints,
        axis = axis,
        xLabels = labels,
        yLabel = { value -> if (metric == TelemetryMetric.TEMPERATURE) "${value.toInt()}°" else "${value.toInt()}" },
        description = stringResource(R.string.metric_chart_description, metricName(metric), latestText),
        stressBelow = if (isSoil) SoilMoistureRules.RECHARGE_POINT_PERCENT else null,
        stressLabel = if (isSoil) stringResource(R.string.metric_chart_stress, rechargeText) else "",
        irrigationFractions = state.irrigationEvents.map { windowFraction(it, state.range, state.now) },
        irrigationLabel = stringResource(R.string.metric_chart_irrigation),
        tooltip = latest?.let { stringResource(R.string.metric_chart_now, latestText) },
    )
}

@Composable
private fun metricName(metric: TelemetryMetric): String = stringResource(
    when (metric) {
        TelemetryMetric.TEMPERATURE -> R.string.climate_temperature
        TelemetryMetric.AIR_HUMIDITY -> R.string.climate_air_humidity
        TelemetryMetric.SOIL_MOISTURE -> R.string.climate_soil_title
    },
)

// ─── Min / average / max ────────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    state: TelemetryDetailUiState.Content,
    min: Double,
    average: Double,
    max: Double,
    locale: Locale,
) {
    val stats = state.stats ?: return
    fun whenLabel(at: Instant): String {
        val date = at.atZone(state.zone).toLocalDate()
        return if (state.range == TelemetryRange.LAST_30_DAYS) formatDayMonth(date, locale) else shortWeekday(date, locale).lowercase(locale)
    }
    val afterIrrigation = state.irrigationEvents.any { event ->
        val gap = Duration.between(event, stats.max.observedAt)
        !gap.isNegative && gap <= AFTER_IRRIGATION_WINDOW
    }
    val maxWhen = if (afterIrrigation) stringResource(R.string.metric_stat_after_irrigation) else whenLabel(stats.max.observedAt)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(
            value = formatMetric(locale, state.metric, min),
            label = stringResource(R.string.metric_stat_min, whenLabel(stats.min.observedAt)),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = formatMetric(locale, state.metric, average),
            label = stringResource(R.string.metric_stat_avg),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            value = formatMetric(locale, state.metric, max),
            label = stringResource(R.string.metric_stat_max, maxWhen),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = value,
            style = TextStyle(fontFamily = NewsreaderFamily, fontSize = 26.sp, lineHeight = 30.sp),
            color = Neutral900,
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Neutral600)
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────────────────────

private fun previewDetail(metric: TelemetryMetric, range: TelemetryRange) = TelemetryDetailUiState.Content(
    metric = metric,
    range = range,
    plotName = "La Yarada 02",
    series = previewSeries(),
    now = PreviewNow,
    zone = PreviewZone,
    lastRefresh = PreviewNow,
    isRefreshing = false,
    refreshError = null,
)

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 1300)
@Composable
private fun SoilMoistureDetailPreview() {
    VioraTheme {
        TelemetryDetailContent(
            state = previewDetail(TelemetryMetric.SOIL_MOISTURE, TelemetryRange.LAST_7_DAYS),
            onBack = {}, onSelectMetric = {}, onSelectRange = {}, onRefresh = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 1200)
@Composable
private fun TemperatureDetailPreview() {
    VioraTheme {
        TelemetryDetailContent(
            state = previewDetail(TelemetryMetric.TEMPERATURE, TelemetryRange.LAST_24_HOURS),
            onBack = {}, onSelectMetric = {}, onSelectRange = {}, onRefresh = {},
        )
    }
}
