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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.phenology.domain.entity.ChillCurvePoint
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState

/**
 * Cumulative chill portions line chart (Erez dynamic model) matching Figma P80.
 * Includes editorial header, top legend, Canvas with "Hoy · 24" callout and projected point,
 * and bottom physiological caption.
 */
@Composable
fun ChillCurveChart(
    curvePoints: List<ChillCurvePoint>,
    threshold: Double = 30.0,
    currentPortions: Double = 24.0,
    seasonState: WinterSeasonState = WinterSeasonState.ACCUMULATING,
    daysAbove24Celsius: Int = 0,
    projectedCompletionDate: LocalDate? = null,
    varietyName: String = "",
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
    val smallLabelStyle = remember {
        TextStyle(
            fontFamily = RobotoFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = Neutral600,
        )
    }
    val badgeTextStyle = remember {
        TextStyle(
            fontFamily = RobotoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Neutral0,
        )
    }

    val junLabel = stringResource(R.string.winter_chill_month_jun)
    val julLabel = stringResource(R.string.winter_chill_month_jul)
    val augLabel = stringResource(R.string.winter_chill_month_aug)
    val augEndLabel = stringResource(R.string.winter_chill_month_aug_end)
    val todayCalloutText = stringResource(R.string.winter_chill_chart_today_callout, currentPortions.toInt())
    val resolvedVariety = varietyName.ifBlank { stringResource(R.string.variety_sevillana) }
    val thresholdVarietyText = stringResource(R.string.winter_chill_chart_threshold_variety, threshold.toInt(), resolvedVariety)

    val fallbackProjDate = LocalDate.of(LocalDate.now().year, 8, 4)
    val projDateLabel = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        .format(projectedCompletionDate ?: fallbackProjDate)
        .replace(".", "")

    val noteText = when (seasonState) {
        WinterSeasonState.ACCUMULATING -> stringResource(R.string.winter_chill_chart_note_accumulating)
        WinterSeasonState.CHILL_HALTED -> stringResource(R.string.winter_chill_chart_note_halted, daysAbove24Celsius)
        WinterSeasonState.COMPLETED -> stringResource(R.string.winter_chill_chart_note_completed, resolvedVariety, projDateLabel)
        WinterSeasonState.OFF_SEASON -> stringResource(R.string.winter_chill_chart_note_off_season)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Section Title: "Frío acumulado" (Newsreader 24sp)
        Text(
            text = stringResource(R.string.winter_chill_chart_section_title),
            style = TextStyle(
                fontFamily = NewsreaderFamily,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.24).sp,
            ),
            color = Neutral900,
        )

        // Card container (white, rounded 28dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Neutral0)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Legend row (top)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChartLegendItem(
                    color = Neutral900,
                    text = stringResource(R.string.winter_chill_chart_legend_current),
                    isDashed = false,
                )
                ChartLegendItem(
                    color = Neutral300,
                    text = stringResource(R.string.winter_chill_chart_legend_past),
                    isDashed = true,
                )
                ChartLegendItem(
                    color = Neutral500,
                    text = stringResource(R.string.winter_chill_chart_legend_threshold, threshold.toInt()),
                    isDashed = true,
                )
            }

            // Canvas Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                val paddingLeft = 32.dp.toPx()
                val paddingBottom = 26.dp.toPx()
                val paddingTop = 16.dp.toPx()
                val paddingRight = 16.dp.toPx()

                val chartWidth = size.width - paddingLeft - paddingRight
                val chartHeight = size.height - paddingTop - paddingBottom
                if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

                val maxY = 35f // headroom above 30
                fun yToPx(value: Float): Float =
                    paddingTop + chartHeight * (1f - (value / maxY).coerceIn(0f, 1f))

                fun xToPx(dayIndex: Float, totalDays: Float = 92f): Float =
                    paddingLeft + chartWidth * (dayIndex / totalDays).coerceIn(0f, 1f)

                // Guides at y=10 and y=20
                listOf(10f, 20f).forEach { step ->
                    val y = yToPx(step)
                    drawLine(
                        color = Neutral200,
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width - paddingRight, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                // Threshold line at y=30 (dashed)
                val thresholdY = yToPx(threshold.toFloat())
                drawLine(
                    color = Neutral500,
                    start = Offset(paddingLeft, thresholdY),
                    end = Offset(size.width - paddingRight, thresholdY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f),
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = thresholdVarietyText,
                    style = smallLabelStyle,
                    topLeft = Offset(paddingLeft, thresholdY - 14.dp.toPx()),
                )

                // Previous Winter Curve points
                val prevCurve = if (curvePoints.isNotEmpty() && curvePoints.any { it.accumulatedPreviousYear != null }) {
                    curvePoints.mapIndexed { idx, pt ->
                        Pair(idx.toFloat() * (92f / curvePoints.size), pt.accumulatedPreviousYear?.toFloat() ?: 0f)
                    }
                } else {
                    listOf(
                        Pair(0f, 0f),
                        Pair(15f, 4f),
                        Pair(30f, 10f),
                        Pair(45f, 18f),
                        Pair(66f, 30f),
                        Pair(92f, 32f),
                    )
                }

                // Draw Previous Winter Curve (dashed Neutral300)
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
                            width = 2.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                        ),
                    )
                }

                // Current Winter Curve points
                val thisCurve = if (curvePoints.isNotEmpty()) {
                    curvePoints.mapIndexed { idx, pt ->
                        Pair(idx.toFloat() * (92f / curvePoints.size), pt.accumulatedThisYear.toFloat())
                    }
                } else {
                    val currentDay = when (seasonState) {
                        WinterSeasonState.OFF_SEASON -> 92f
                        WinterSeasonState.COMPLETED -> 65f
                        else -> 55f
                    }
                    listOf(
                        Pair(0f, 0f),
                        Pair(15f, 3f),
                        Pair(30f, 8f),
                        Pair(45f, 16f),
                        Pair(currentDay, currentPortions.toFloat()),
                    )
                }

                // Draw Shaded Area under this curve
                if (thisCurve.size >= 2) {
                    val areaPath = Path().apply {
                        moveTo(xToPx(thisCurve[0].first), yToPx(0f))
                        for (pt in thisCurve) {
                            lineTo(xToPx(pt.first), yToPx(pt.second))
                        }
                        lineTo(xToPx(thisCurve.last().first), yToPx(0f))
                        close()
                    }
                    drawPath(
                        path = areaPath,
                        color = Neutral200.copy(alpha = 0.45f),
                        style = Fill,
                    )

                    // Draw Solid This Winter Path
                    val thisPath = Path().apply {
                        moveTo(xToPx(thisCurve[0].first), yToPx(thisCurve[0].second))
                        for (i in 1 until thisCurve.size) {
                            lineTo(xToPx(thisCurve[i].first), yToPx(thisCurve[i].second))
                        }
                    }
                    drawPath(
                        path = thisPath,
                        color = Neutral900,
                        style = Stroke(width = 2.5.dp.toPx()),
                    )
                }

                // Projection curve from last point to threshold
                val lastPt = thisCurve.lastOrNull() ?: Pair(55f, currentPortions.toFloat())
                val projDay = 65f // ~August 4
                if (seasonState != WinterSeasonState.OFF_SEASON && lastPt.second < threshold.toFloat()) {
                    val projPath = Path().apply {
                        moveTo(xToPx(lastPt.first), yToPx(lastPt.second))
                        lineTo(xToPx(projDay), thresholdY)
                    }
                    drawPath(
                        path = projPath,
                        color = Neutral900,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                        ),
                    )

                    // Projected finish point dot (white with black border)
                    val projX = xToPx(projDay)
                    drawCircle(color = Neutral900, radius = 5.dp.toPx(), center = Offset(projX, thresholdY))
                    drawCircle(color = Neutral0, radius = 3.dp.toPx(), center = Offset(projX, thresholdY))

                    // Projected date label above
                    val projTextLayout = textMeasurer.measure(projDateLabel, smallLabelStyle)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = projDateLabel,
                        style = smallLabelStyle,
                        topLeft = Offset(projX - projTextLayout.size.width / 2f, thresholdY - 14.dp.toPx()),
                    )
                }

                // Today marker (vertical line + dot + floating badge)
                val todayX = xToPx(lastPt.first)
                val todayY = yToPx(lastPt.second)

                // Vertical line
                drawLine(
                    color = Neutral900.copy(alpha = 0.35f),
                    start = Offset(todayX, todayY),
                    end = Offset(todayX, yToPx(0f)),
                    strokeWidth = 1.dp.toPx(),
                )

                // Outer and inner circle for today
                drawCircle(color = Neutral0, radius = 6.dp.toPx(), center = Offset(todayX, todayY))
                drawCircle(color = Neutral900, radius = 4.dp.toPx(), center = Offset(todayX, todayY))

                // Floating badge: "Hoy · 24"
                val badgeLayout = textMeasurer.measure(todayCalloutText, badgeTextStyle)
                val badgeWidth = badgeLayout.size.width + 16.dp.toPx()
                val badgeHeight = badgeLayout.size.height + 6.dp.toPx()
                val badgeLeft = (todayX - badgeWidth - 8.dp.toPx()).coerceAtLeast(paddingLeft)
                val badgeTop = todayY - badgeHeight / 2f

                drawRoundRect(
                    color = Neutral900,
                    topLeft = Offset(badgeLeft, badgeTop),
                    size = Size(badgeWidth, badgeHeight),
                    cornerRadius = CornerRadius(100f, 100f),
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = todayCalloutText,
                    style = badgeTextStyle,
                    topLeft = Offset(badgeLeft + 8.dp.toPx(), badgeTop + 3.dp.toPx()),
                )

                // X-axis month labels (Jun, Jul, Ago, 31 ago)
                val months = listOf(
                    Pair(0f, junLabel),
                    Pair(30f, julLabel),
                    Pair(61f, augLabel),
                    Pair(92f, augEndLabel),
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
            }

            // Bottom educational note
            Text(
                text = noteText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                color = Neutral600,
            )
        }
    }
}

@Composable
private fun ChartLegendItem(
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
                    .size(width = 14.dp, height = 2.5.dp)
                    .background(color, RoundedCornerShape(1.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 2.5.dp)
                    .background(color, RoundedCornerShape(1.dp)),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Neutral700,
            maxLines = 1,
            softWrap = false,
        )
    }
}

