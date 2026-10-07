package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.telemetry.domain.entity.DailyThermalSummary

/** A small trend line without axes: the cards of the plot sensors (Figma P90). */
@Composable
internal fun Sparkline(values: List<Double>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val lo = values.min()
        val span = (values.max() - lo).takeIf { it > MIN_SPAN } ?: 1.0
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height * (1f - SPARK_MARGIN) - ((value - lo) / span).toFloat() * size.height * (1f - 2 * SPARK_MARGIN)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private const val MIN_SPAN = 1e-6
private const val SPARK_MARGIN = 0.1f

private const val GAUGE_MAX_PERCENT = 50.0

/**
 * The vertical gauge of the soil probe: a capsule filled up to [percent], a knob at the level
 * and a terracotta line at the [rechargePercent] below which the soil is under stress.
 */
@Composable
internal fun SoilGauge(
    percent: Double,
    rechargePercent: Double,
    trackColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val trackWidth = size.width * 0.5f
        val left = (size.width - trackWidth) / 2
        val corner = CornerRadius(trackWidth / 2)
        fun yOf(value: Double) = size.height * (1f - (value / GAUGE_MAX_PERCENT).toFloat().coerceIn(0f, 1f))

        drawRoundRect(trackColor, Offset(left, 0f), Size(trackWidth, size.height), corner)

        val levelY = yOf(percent)
        drawRoundRect(Green900, Offset(left, levelY), Size(trackWidth, size.height - levelY), corner)

        val knobRadius = trackWidth * 0.36f
        val knobY = levelY.coerceIn(knobRadius, size.height - knobRadius)
        drawCircle(Neutral0, knobRadius, Offset(size.width / 2, knobY))
        drawCircle(Green900, knobRadius, Offset(size.width / 2, knobY), style = Stroke(2.dp.toPx()))

        val rechargeY = yOf(rechargePercent)
        val overhang = 5.dp.toPx()
        drawLine(
            color = Terracotta500,
            start = Offset(left - overhang, rechargeY),
            end = Offset(left + trackWidth + overhang, rechargeY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

/**
 * Day and night temperature of each day (Figma P90 "Día y noche"; US17, scenario 2): a vertical
 * stem from the night average (dark dot) up to the day average (yellow dot), labelled by weekday.
 */
@Composable
internal fun DayNightChart(days: List<DailyThermalSummary>, locale: Locale, modifier: Modifier = Modifier) {
    val values = days.flatMap { listOfNotNull(it.dayAverageCelsius, it.nightAverageCelsius) }
    if (values.isEmpty()) return
    val axis = ChartAxis.nice(values.min(), values.max(), lines = 3)

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEach { day ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Canvas(Modifier.fillMaxWidth().height(DAY_NIGHT_HEIGHT)) {
                    val pad = DOT_RADIUS.toPx() + 2.dp.toPx()
                    fun yOf(value: Double) = pad + (1f - axis.fractionOf(value)) * (size.height - 2 * pad)
                    val x = size.width / 2
                    val dayY = day.dayAverageCelsius?.let(::yOf)
                    val nightY = day.nightAverageCelsius?.let(::yOf)
                    if (dayY != null && nightY != null) {
                        drawLine(Neutral300, Offset(x, dayY), Offset(x, nightY), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                    }
                    if (dayY != null) drawCircle(Harvest300, DOT_RADIUS.toPx(), Offset(x, dayY))
                    if (nightY != null) drawCircle(Green900, DOT_RADIUS.toPx(), Offset(x, nightY))
                }
                Text(
                    text = shortWeekday(day.date, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600,
                )
            }
        }
    }
}

private val DAY_NIGHT_HEIGHT = 132.dp
private val DOT_RADIUS = 5.dp

/** One point of [MetricLineChart]: [fraction] is its place on the time axis (0 = start, 1 = now). */
internal data class ChartPoint(val fraction: Float, val value: Double)

/**
 * The curve of a metric over a window (Figma P91). Draws gridlines with their values, an optional
 * stress band below [stressBelow], a drop marker at every irrigation, the line with its area and a
 * pill on the last value. The time labels come from [timeAxisLabels].
 */
@Composable
internal fun MetricLineChart(
    points: List<ChartPoint>,
    axis: ChartAxis,
    xLabels: List<AxisLabel>,
    yLabel: (Double) -> String,
    description: String,
    modifier: Modifier = Modifier,
    stressBelow: Double? = null,
    stressLabel: String = "",
    irrigationFractions: List<Float> = emptyList(),
    irrigationLabel: String = "",
    tooltip: String? = null,
) {
    val measurer = rememberTextMeasurer()
    val axisStyle = TextStyle(fontFamily = RobotoFamily, fontSize = 11.sp, color = Neutral600)
    val stressStyle = TextStyle(
        fontFamily = RobotoFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Terracotta700,
    )
    val tooltipStyle = TextStyle(
        fontFamily = RobotoFamily, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Neutral50,
    )

    Canvas(modifier.fillMaxWidth().height(CHART_HEIGHT).semantics { contentDescription = description }) {
        val left = 40.dp.toPx()
        val right = 14.dp.toPx()
        val top = 34.dp.toPx()
        val bottom = 28.dp.toPx()
        val plotWidth = size.width - left - right
        val plotHeight = size.height - top - bottom
        fun xOf(fraction: Float) = left + fraction * plotWidth
        fun yOf(value: Double) = top + (1f - axis.fractionOf(value)) * plotHeight

        if (stressBelow != null && stressBelow > axis.min) {
            val bandTop = yOf(stressBelow)
            drawRect(Terracotta100, Offset(left, bandTop), Size(plotWidth, top + plotHeight - bandTop))
            if (stressLabel.isNotEmpty()) {
                val label = measurer.measure(stressLabel, stressStyle)
                drawText(
                    measurer, stressLabel,
                    Offset(left + 8.dp.toPx(), top + plotHeight - label.size.height - 6.dp.toPx()),
                    stressStyle,
                )
            }
        }

        val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        axis.gridValues.forEach { value ->
            val y = yOf(value)
            drawLine(Neutral200, Offset(left, y), Offset(left + plotWidth, y), strokeWidth = 1.dp.toPx(), pathEffect = dash)
            val label = measurer.measure(yLabel(value), axisStyle)
            drawText(measurer, yLabel(value), Offset(0f, y - label.size.height / 2f), axisStyle)
        }

        val ordered = points.sortedBy { it.fraction }
        if (ordered.size >= 2) {
            val line = Path()
            ordered.forEachIndexed { index, point ->
                val x = xOf(point.fraction)
                val y = yOf(point.value)
                if (index == 0) line.moveTo(x, y) else line.lineTo(x, y)
            }
            val area = Path().apply {
                addPath(line)
                lineTo(xOf(ordered.last().fraction), top + plotHeight)
                lineTo(xOf(ordered.first().fraction), top + plotHeight)
                close()
            }
            drawPath(area, Brush.verticalGradient(listOf(Green900.copy(alpha = 0.16f), Color.Transparent), top, top + plotHeight))
            drawPath(line, Green900, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        irrigationFractions.forEach { fraction ->
            val x = xOf(fraction)
            drawLine(Harvest400, Offset(x, top - 6.dp.toPx()), Offset(x, top + plotHeight), strokeWidth = 1.dp.toPx(), pathEffect = dash)
            drawDrop(Offset(x, top - 14.dp.toPx()), 5.dp.toPx(), Harvest400)
            if (irrigationLabel.isNotEmpty() && irrigationFractions.size <= MAX_LABELLED_IRRIGATIONS) {
                drawText(measurer, irrigationLabel, Offset(x + 9.dp.toPx(), top - 24.dp.toPx()), axisStyle)
            }
        }

        ordered.lastOrNull()?.let { last ->
            val center = Offset(xOf(last.fraction), yOf(last.value))
            drawCircle(Neutral0, 7.dp.toPx(), center)
            drawCircle(Green900, 5.dp.toPx(), center)
            if (tooltip != null) {
                val text = measurer.measure(tooltip, tooltipStyle)
                val padX = 10.dp.toPx()
                val padY = 5.dp.toPx()
                val pillWidth = text.size.width + 2 * padX
                val pillHeight = text.size.height + 2 * padY
                val pillLeft = (center.x + 8.dp.toPx() - pillWidth).coerceIn(0f, size.width - pillWidth)
                val above = center.y - 12.dp.toPx() - pillHeight
                val pillTop = if (above >= 0f) above else center.y + 12.dp.toPx()
                drawRoundRect(Green900, Offset(pillLeft, pillTop), Size(pillWidth, pillHeight), CornerRadius(pillHeight / 2))
                drawText(measurer, tooltip, Offset(pillLeft + padX, pillTop + padY), tooltipStyle)
            }
        }

        xLabels.forEach { label ->
            val text = measurer.measure(label.text, axisStyle)
            val x = (xOf(label.fraction) - text.size.width / 2f).coerceIn(0f, size.width - text.size.width)
            drawText(measurer, label.text, Offset(x, top + plotHeight + 8.dp.toPx()), axisStyle)
        }
    }
}

/** A small water drop (circle with a point on top) centred at [center]. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDrop(center: Offset, radius: Float, color: Color) {
    val body = Offset(center.x, center.y + radius * 0.35f)
    drawCircle(color, radius, body)
    val tip = Path().apply {
        moveTo(center.x, center.y - radius * 1.5f)
        lineTo(center.x - radius * 0.85f, body.y - radius * 0.35f)
        lineTo(center.x + radius * 0.85f, body.y - radius * 0.35f)
        close()
    }
    drawPath(tip, color)
}

private val CHART_HEIGHT = 250.dp
private const val MAX_LABELLED_IRRIGATIONS = 2
