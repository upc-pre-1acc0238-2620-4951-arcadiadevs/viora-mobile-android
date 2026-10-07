package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.features.phenology.domain.entity.ChillCurvePoint

/**
 * Cumulative chill portions line chart (Erez dynamic model).
 * Plots cumulative curve from June 1 to August 31 (92 days) against the 30-portion threshold.
 */
@Composable
fun ChillCurveChart(
    curvePoints: List<ChillCurvePoint>,
    threshold: Double = 30.0,
    currentPortions: Double = 24.0,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val axisTextStyle = remember {
        TextStyle(
            fontFamily = RobotoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = Neutral600,
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Neutral0)
            .border(1.dp, Neutral200, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.winter_chill_curve_title),
                style = MaterialTheme.typography.titleMedium,
                color = Neutral900,
            )
            Text(
                text = "Umbral: ${threshold.toInt()}",
                style = MaterialTheme.typography.labelMedium,
                color = Neutral600,
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            val paddingLeft = 32.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val paddingTop = 12.dp.toPx()
            val paddingRight = 16.dp.toPx()

            val chartWidth = size.width - paddingLeft - paddingRight
            val chartHeight = size.height - paddingTop - paddingBottom
            if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

            val maxY = 35f // Allow headroom above threshold 30
            fun yToPx(value: Float): Float =
                paddingTop + chartHeight * (1f - (value / maxY).coerceIn(0f, 1f))

            fun xToPx(dayIndex: Float, totalDays: Float = 92f): Float =
                paddingLeft + chartWidth * (dayIndex / totalDays).coerceIn(0f, 1f)

            // Y-axis grid lines and labels (0, 15, 30)
            val ySteps = listOf(0f, 15f, 30f)
            ySteps.forEach { step ->
                val y = yToPx(step)
                drawLine(
                    color = Neutral200,
                    start = Offset(paddingLeft, y),
                    end = Offset(size.width - paddingRight, y),
                    strokeWidth = 1.dp.toPx(),
                )
                val textLayout = textMeasurer.measure(step.toInt().toString(), axisTextStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = step.toInt().toString(),
                    style = axisTextStyle,
                    topLeft = Offset(paddingLeft - textLayout.size.width - 8.dp.toPx(), y - textLayout.size.height / 2f),
                )
            }

            // X-axis month labels (Jun, Jul, Ago)
            val months = listOf(
                Pair(0f, "Jun"),
                Pair(30f, "Jul"),
                Pair(61f, "Ago"),
                Pair(92f, "31 Ago"),
            )
            months.forEach { (day, label) ->
                val x = xToPx(day)
                val textLayout = textMeasurer.measure(label, axisTextStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = axisTextStyle,
                    topLeft = Offset(x - textLayout.size.width / 2f, size.height - paddingBottom + 6.dp.toPx()),
                )
            }

            // Threshold dashed horizontal line (30 portions)
            val thresholdY = yToPx(threshold.toFloat())
            drawLine(
                color = Green800,
                start = Offset(paddingLeft, thresholdY),
                end = Offset(size.width - paddingRight, thresholdY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
            )

            // Synthetic or provided curve points
            val prevCurve = if (curvePoints.isNotEmpty() && curvePoints.any { it.accumulatedPreviousYear != null }) {
                curvePoints.mapIndexed { idx, pt ->
                    Pair(idx.toFloat() * (92f / curvePoints.size), pt.accumulatedPreviousYear?.toFloat() ?: 0f)
                }
            } else {
                // Baseline curve for previous winter reaching 30 by Aug 5 (~day 66)
                listOf(
                    Pair(0f, 0f),
                    Pair(15f, 4f),
                    Pair(30f, 10f),
                    Pair(45f, 18f),
                    Pair(66f, 30f),
                    Pair(92f, 32f),
                )
            }

            val thisCurve = if (curvePoints.isNotEmpty()) {
                curvePoints.mapIndexed { idx, pt ->
                    Pair(idx.toFloat() * (92f / curvePoints.size), pt.accumulatedThisYear.toFloat())
                }
            } else {
                // Baseline curve for this winter reaching 24 around day 60
                listOf(
                    Pair(0f, 0f),
                    Pair(15f, 3f),
                    Pair(30f, 8f),
                    Pair(45f, 15f),
                    Pair(60f, currentPortions.toFloat()),
                )
            }

            // Draw Previous Winter Curve (dotted Neutral400)
            if (prevCurve.size >= 2) {
                val prevPath = Path().apply {
                    moveTo(xToPx(prevCurve[0].first), yToPx(prevCurve[0].second))
                    for (i in 1 until prevCurve.size) {
                        lineTo(xToPx(prevCurve[i].first), yToPx(prevCurve[i].second))
                    }
                }
                drawPath(
                    path = prevPath,
                    color = Neutral300,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                    ),
                )
            }

            // Draw This Winter Curve (Solid Harvest800)
            if (thisCurve.size >= 2) {
                val thisPath = Path().apply {
                    moveTo(xToPx(thisCurve[0].first), yToPx(thisCurve[0].second))
                    for (i in 1 until thisCurve.size) {
                        lineTo(xToPx(thisCurve[i].first), yToPx(thisCurve[i].second))
                    }
                }
                drawPath(
                    path = thisPath,
                    color = Harvest800,
                    style = Stroke(width = 3.dp.toPx()),
                )

                // Current point indicator circle
                val lastPoint = thisCurve.last()
                val lastX = xToPx(lastPoint.first)
                val lastY = yToPx(lastPoint.second)
                drawCircle(color = Harvest800, radius = 5.dp.toPx(), center = Offset(lastX, lastY))
                drawCircle(color = Neutral0, radius = 2.5.dp.toPx(), center = Offset(lastX, lastY))
            }
        }

        // Legend row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendItem(
                color = Harvest800,
                text = stringResource(R.string.winter_chill_curve_legend_this_year),
                isDashed = false,
            )
            LegendItem(
                color = Neutral300,
                text = stringResource(R.string.winter_chill_curve_legend_last_year),
                isDashed = true,
            )
            LegendItem(
                color = Green800,
                text = stringResource(R.string.winter_chill_curve_legend_threshold),
                isDashed = true,
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String,
    isDashed: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isDashed) {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 3.dp)
                    .background(color, RoundedCornerShape(1.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Neutral700,
        )
    }
}
