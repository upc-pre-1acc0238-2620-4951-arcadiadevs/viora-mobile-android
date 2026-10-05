package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.phenology.domain.entity.BbiInterval
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord

/** Tag colours of a campaign: ON dark green, OFF harvest yellow, anything else neutral. */
fun BearingYear.barColor(): Color = when (this) {
    BearingYear.ON -> Green800
    BearingYear.OFF -> Harvest300
    BearingYear.BALANCED, BearingYear.UNKNOWN -> Neutral300
}

private fun BearingYear.tagBackground(): Color = when (this) {
    BearingYear.ON -> Green200
    BearingYear.OFF -> Harvest100
    BearingYear.BALANCED, BearingYear.UNKNOWN -> Neutral100
}

private fun BearingYear.tagTextRes(): Int? = when (this) {
    BearingYear.ON -> R.string.harvest_tag_on
    BearingYear.OFF -> R.string.harvest_tag_off
    BearingYear.BALANCED -> R.string.harvest_tag_balanced
    BearingYear.UNKNOWN -> null
}

/** ON / OFF pill used in the chart and in the list. */
@Composable
fun BearingTag(bearing: BearingYear, modifier: Modifier = Modifier, large: Boolean = false, onHighlight: Boolean = false) {
    val label = bearing.tagTextRes()?.let { stringResource(it) } ?: return
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = if (large) 11.sp else 10.sp, lineHeight = if (large) 14.sp else 12.sp),
        color = if (bearing == BearingYear.ON) Green900 else Neutral900,
        modifier = modifier
            .clip(CircleShape)
            .background(if (onHighlight) Neutral0 else bearing.tagBackground())
            .padding(horizontal = if (large) 8.dp else 6.dp, vertical = if (large) 3.dp else 2.dp),
    )
}

private val BarWidth = 44.dp
private val MinSlot = 56.dp
private val TrackHeight = 180.dp
private val ChartHeight = 212.dp
private val TrackTop = 24.dp

/** Shows a harvest per hectare when the plot area is known, else in kilograms. */
private class YieldScale(private val areaHectares: Double?) {
    fun value(kg: Double): Double = if (areaHectares != null) tonnesPerHectare(kg, areaHectares) else kg
    fun label(kg: Double): String = if (areaHectares != null) formatTonnesPerHectare(value(kg)) else formatKgCompact(kg)
}

/**
 * The yield per campaign (Figma P40 "Serie ON / OFF"): a pill bar per campaign over a light track,
 * green for an ON year and yellow for an OFF year, the alternation line through the bar tops and
 * the dashed average. Years and tags sit under the bars; the interval chips below. The campaigns
 * still needed for an index ([missingYears], oldest first) are dashed columns with a "+" that
 * calls [onAddYear]. Values are tonnes per hectare when [areaHectares] is known.
 * [records] come in any order; the chart shows them oldest to newest.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HarvestChartCard(
    records: List<HarvestRecord>,
    averageKg: Double,
    intervals: List<BbiInterval>,
    modifier: Modifier = Modifier,
    areaHectares: Double? = null,
    missingYears: List<Int> = emptyList(),
    enabled: Boolean = true,
    onAddYear: (Int) -> Unit = {},
) {
    val ordered = remember(records) { records.sortedBy { it.campaignYear } }
    val scale = remember(areaHectares) { YieldScale(areaHectares) }
    val description = chartDescription(ordered, averageKg, areaHectares)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LegendDot(Green800, stringResource(R.string.harvest_year_on))
            LegendDot(Harvest300, stringResource(R.string.harvest_year_off))
            if (ordered.size > 1) {
                LegendDash(
                    if (areaHectares != null) {
                        stringResource(R.string.harvest_average_ha, formatTonnesPerHectare(scale.value(averageKg)))
                    } else {
                        stringResource(R.string.harvest_average, formatKg(averageKg))
                    },
                )
            }
        }

        val columns = missingYears.size + ordered.size
        if (columns > 0) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val slot = max(maxWidth.value / columns, MinSlot.value).dp
                val scroll = rememberScrollState()
                LaunchedEffect(scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }
                Column(modifier = Modifier.horizontalScroll(scroll)) {
                    Box {
                        Box(Modifier.clearAndSetSemantics { if (ordered.isNotEmpty()) contentDescription = description }) {
                            BarsCanvas(ordered, missingYears.size, averageKg, slot, scale)
                        }
                        Row {
                            missingYears.forEach { year ->
                                Box(Modifier.width(slot).height(ChartHeight), contentAlignment = Alignment.BottomCenter) {
                                    AddYearButton(year = year, enabled = enabled, onClick = { onAddYear(year) })
                                }
                            }
                        }
                    }
                    Row(Modifier.clearAndSetSemantics { }) {
                        missingYears.forEach { year ->
                            YearLabel(year = year, modifier = Modifier.width(slot)) { MissingTag() }
                        }
                        ordered.forEach { record ->
                            YearLabel(year = record.campaignYear, modifier = Modifier.width(slot)) { BearingTag(record.bearing) }
                        }
                    }
                }
            }
        }

        if (intervals.isEmpty()) {
            Text(
                text = stringResource(R.string.harvest_intervals_later),
                style = MaterialTheme.typography.labelMedium,
                color = Neutral700,
            )
        } else {
            IntervalChips(intervals)
        }
    }
}

@Composable
private fun YearLabel(year: Int, modifier: Modifier = Modifier, tag: @Composable () -> Unit) {
    Column(
        modifier = modifier.padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = year.toString(), style = MaterialTheme.typography.bodySmall, color = Neutral700)
        tag()
    }
}

/** Dashed "falta" pill of a campaign that is still to be registered. */
@Composable
fun MissingTag(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.harvest_missing_tag),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
        color = Neutral700,
        modifier = modifier
            .dashedOutline(Neutral300, CornerRadius(100f, 100f), strokeWidth = 1.dp, dash = 2.dp)
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

/** Dashed rounded outline, for what is missing and can be added. */
fun Modifier.dashedOutline(color: Color, corner: CornerRadius, strokeWidth: Dp, dash: Dp, gap: Dp = dash): Modifier =
    drawBehind {
        val stroke = strokeWidth.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(minOf(corner.x, size.height / 2), minOf(corner.y, size.height / 2)),
            style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash.toPx(), gap.toPx()))),
        )
    }

@Composable
private fun AddYearButton(year: Int, enabled: Boolean, onClick: () -> Unit) {
    val label = stringResource(R.string.harvest_missing_add, year)
    Box(
        modifier = Modifier
            .padding(bottom = (ChartHeight - TrackTop - TrackHeight) + 12.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) Green800 else Neutral300)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.ic_add), contentDescription = label, tint = Neutral0, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun chartDescription(ordered: List<HarvestRecord>, averageKg: Double, areaHectares: Double?): String {
    if (ordered.isEmpty()) return ""
    val scale = YieldScale(areaHectares)
    val items = ordered.map { record ->
        if (areaHectares != null) {
            stringResource(R.string.harvest_chart_item_ha, record.campaignYear, formatTonnesPerHectare(scale.value(record.totalYieldKg)))
        } else {
            stringResource(R.string.harvest_chart_item, record.campaignYear, formatKg(record.totalYieldKg))
        }
    }.joinToString(separator = "; ")
    val count = pluralStringResource(R.plurals.harvest_campaigns, ordered.size, ordered.size)
    return if (areaHectares != null) {
        stringResource(R.string.harvest_chart_description_ha, count, items, formatTonnesPerHectare(scale.value(averageKg)))
    } else {
        stringResource(R.string.harvest_chart_description, count, items, formatKg(averageKg))
    }
}

@Composable
private fun BarsCanvas(ordered: List<HarvestRecord>, missingCount: Int, averageKg: Double, slot: Dp, scale: YieldScale) {
    val measurer = rememberTextMeasurer()
    val valueStyle = TextStyle(fontFamily = RobotoFamily, fontSize = 13.sp, textAlign = TextAlign.Center)
    val ghostStyle = TextStyle(fontFamily = RobotoFamily, fontSize = 28.sp, color = Neutral600, textAlign = TextAlign.Center)
    val scaleMax = ((ordered.maxOfOrNull { scale.value(it.totalYieldKg) } ?: 1.0) * 1.04).coerceAtLeast(0.001)
    Canvas(Modifier.width(slot * (missingCount + ordered.size)).height(ChartHeight)) {
        val slotPx = slot.toPx()
        val barPx = BarWidth.toPx()
        val trackH = TrackHeight.toPx()
        val trackTop = TrackTop.toPx()
        val trackBottom = trackTop + trackH
        val radius = CornerRadius(barPx / 2, barPx / 2)

        repeat(missingCount) { i ->
            val centerX = slotPx * i + slotPx / 2
            val left = centerX - barPx / 2
            drawRoundRect(
                color = Neutral300,
                topLeft = Offset(left + 1.dp.toPx(), trackTop + 1.dp.toPx()),
                size = Size(barPx - 2.dp.toPx(), trackH - 2.dp.toPx()),
                cornerRadius = radius,
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
            )
            val mark = measurer.measure("?", ghostStyle)
            drawText(mark, topLeft = Offset(centerX - mark.size.width / 2f, trackTop + trackH * 0.42f - mark.size.height / 2f))
        }

        val tops = ordered.mapIndexed { i, record ->
            val centerX = slotPx * (missingCount + i) + slotPx / 2
            val barH = (scale.value(record.totalYieldKg) / scaleMax * trackH).toFloat().coerceAtLeast(barPx)
            val left = centerX - barPx / 2
            drawRoundRect(Neutral100, Offset(left, trackTop), Size(barPx, trackH), radius)
            drawRoundRect(record.bearing.barColor(), Offset(left, trackBottom - barH), Size(barPx, barH), radius)
            val label = measurer.measure(
                scale.label(record.totalYieldKg),
                valueStyle.copy(color = if (record.bearing == BearingYear.ON) Neutral0 else Neutral900),
            )
            drawText(label, topLeft = Offset(centerX - label.size.width / 2f, trackBottom - 12.dp.toPx() - label.size.height))
            Offset(centerX, trackBottom - barH)
        }

        if (ordered.isNotEmpty()) {
            val averageY = trackBottom - (scale.value(averageKg) / scaleMax * trackH).toFloat()
            drawLine(
                color = Neutral600,
                start = Offset(0f, averageY),
                end = Offset(size.width, averageY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
            )
        }

        if (tops.size > 1) {
            val line = Path().apply {
                moveTo(tops.first().x, tops.first().y)
                tops.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(line, Terracotta500, style = Stroke(width = 2.dp.toPx()))
        }
        tops.forEach { top ->
            drawCircle(Neutral0, radius = 6.dp.toPx(), center = top)
            drawCircle(Terracotta500, radius = 6.dp.toPx(), center = top, style = Stroke(width = 2.dp.toPx()))
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal), color = Neutral700)
    }
}

@Composable
private fun LegendDash(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.width(12.dp).height(2.dp)) {
            drawLine(
                Neutral600,
                Offset(0f, size.height / 2),
                Offset(size.width, size.height / 2),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())),
            )
        }
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal), color = Neutral700)
    }
}

@Composable
private fun IntervalChips(intervals: List<BbiInterval>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.harvest_intervals_title),
            style = MaterialTheme.typography.labelMedium,
            color = Neutral600,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            intervals.forEach { interval ->
                Column(
                    modifier = Modifier
                        .widthIn(min = 98.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Neutral100)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = formatIndex(interval.value),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Neutral900,
                    )
                    Text(
                        text = stringResource(R.string.harvest_interval_label, interval.fromYear % 100, interval.toYear % 100),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                        color = Neutral600,
                    )
                }
            }
        }
    }
}
