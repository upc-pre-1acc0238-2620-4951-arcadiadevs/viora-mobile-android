package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.floor
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.phenology.domain.entity.ChillCurvePoint
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState

/**
 * Cumulative chill portions chart (Dynamic Model) matching Figma P80: this winter (solid), the previous winter
 * (dashed), the varietal threshold and, while the season is open, the projection to the threshold. Only the days
 * the backend evaluated are drawn, each at its own date; with no data the chart says so instead of drawing a curve.
 */
@Composable
fun ChillCurveChart(
    curvePoints: List<ChillCurvePoint>,
    threshold: Double,
    seasonState: WinterSeasonState,
    currentWarmStreakDays: Int,
    completionDate: LocalDate?,
    projectedCompletionDate: LocalDate?,
    varietyName: String,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val locale = LocalConfiguration.current.locales[0]
    val axisTextStyle = remember {
        TextStyle(
            fontFamily = RobotoFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = Neutral600,
        )
    }
    val thresholdLabelStyle = remember {
        TextStyle(
            fontFamily = RobotoFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = Terracotta700,
        )
    }
    val projLabelStyle = remember {
        TextStyle(
            fontFamily = RobotoFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            color = Green800,
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

    val seasonStart = curvePoints.firstOrNull()?.date
    val current = curvePoints.mapNotNull { point -> point.accumulatedThisYear?.let { point.date to it } }
    val previous = curvePoints.mapNotNull { point -> point.accumulatedPreviousYear?.let { point.date to it } }
    val lastCurrent = current.lastOrNull()
    val seasonOpen = seasonState != WinterSeasonState.OFF_SEASON

    val junLabel = stringResource(R.string.winter_chill_month_jun)
    val julLabel = stringResource(R.string.winter_chill_month_jul)
    val augLabel = stringResource(R.string.winter_chill_month_aug)
    val augEndLabel = stringResource(R.string.winter_chill_month_aug_end)
    val todayCalloutText = stringResource(
        R.string.winter_chill_chart_today_callout,
        floor(lastCurrent?.second ?: 0.0).toInt(),
    )
    val thresholdText = if (varietyName.isBlank()) {
        stringResource(R.string.winter_chill_chart_legend_threshold, threshold.toInt())
    } else {
        stringResource(R.string.winter_chill_chart_threshold_variety, threshold.toInt(), varietyName)
    }
    val projDateLabel = projectedCompletionDate?.let { formatChartDate(it, locale) }

    val noteText = when (seasonState) {
        WinterSeasonState.ACCUMULATING -> stringResource(R.string.winter_chill_chart_note_accumulating)
        WinterSeasonState.CHILL_HALTED -> stringResource(R.string.winter_chill_chart_note_halted, currentWarmStreakDays)
        WinterSeasonState.COMPLETED -> if (completionDate != null && varietyName.isNotBlank()) {
            stringResource(R.string.winter_chill_chart_note_completed, varietyName, formatChartDate(completionDate, locale))
        } else {
            stringResource(R.string.winter_chill_chart_note_accumulating)
        }
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
                ChartLegendItem(color = Green800, text = stringResource(R.string.winter_chill_chart_legend_current))
                ChartLegendItem(color = Neutral300, text = stringResource(R.string.winter_chill_chart_legend_past))
                ChartLegendItem(
                    color = Terracotta500,
                    text = stringResource(R.string.winter_chill_chart_legend_threshold, threshold.toInt()),
                )
            }

            if (seasonStart == null || (current.isEmpty() && previous.isEmpty())) {
                Text(
                    text = stringResource(R.string.winter_chill_chart_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
            } else {
                val totalDays = (curvePoints.size - 1).coerceAtLeast(1).toFloat()
                val highest = (current.map { it.second } + previous.map { it.second }).maxOrNull() ?: 0.0
                val maxY = (maxOf(threshold, highest) * 1.15).toFloat()

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

                    fun yToPx(value: Double): Float =
                        paddingTop + chartHeight * (1f - (value.toFloat() / maxY).coerceIn(0f, 1f))

                    fun xToPx(date: LocalDate): Float {
                        val day = ChronoUnit.DAYS.between(seasonStart, date).toFloat()
                        return paddingLeft + chartWidth * (day / totalDays).coerceIn(0f, 1f)
                    }

                    // Guides at one and two thirds of the threshold
                    listOf(threshold / 3.0, threshold * 2.0 / 3.0).forEach { step ->
                        val y = yToPx(step)
                        drawLine(
                            color = Neutral200,
                            start = Offset(paddingLeft, y),
                            end = Offset(size.width - paddingRight, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    // Threshold line (dashed)
                    val thresholdY = yToPx(threshold)
                    drawLine(
                        color = Terracotta500,
                        start = Offset(paddingLeft, thresholdY),
                        end = Offset(size.width - paddingRight, thresholdY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f),
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = thresholdText,
                        style = thresholdLabelStyle,
                        topLeft = Offset(paddingLeft, thresholdY - 14.dp.toPx()),
                    )

                    // Previous winter (dashed), aligned by day of the season
                    if (previous.size >= 2) {
                        val prevPath = Path().apply {
                            moveTo(xToPx(previous.first().first), yToPx(previous.first().second))
                            previous.drop(1).forEach { (date, value) -> lineTo(xToPx(date), yToPx(value)) }
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

                    // This winter: shaded area and solid line
                    if (current.size >= 2) {
                        val areaPath = Path().apply {
                            moveTo(xToPx(current.first().first), yToPx(0.0))
                            current.forEach { (date, value) -> lineTo(xToPx(date), yToPx(value)) }
                            lineTo(xToPx(current.last().first), yToPx(0.0))
                            close()
                        }
                        drawPath(path = areaPath, color = Green800.copy(alpha = 0.12f), style = Fill)

                        val thisPath = Path().apply {
                            moveTo(xToPx(current.first().first), yToPx(current.first().second))
                            current.drop(1).forEach { (date, value) -> lineTo(xToPx(date), yToPx(value)) }
                        }
                        drawPath(path = thisPath, color = Green800, style = Stroke(width = 2.5.dp.toPx()))
                    }

                    if (lastCurrent != null) {
                        val lastX = xToPx(lastCurrent.first)
                        val lastY = yToPx(lastCurrent.second)

                        // Projection from the last evaluated day to the projected date on the threshold
                        if (seasonOpen && projectedCompletionDate != null && projDateLabel != null) {
                            val projX = xToPx(projectedCompletionDate)
                            drawLine(
                                color = Green800,
                                start = Offset(lastX, lastY),
                                end = Offset(projX, thresholdY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                            )
                            drawCircle(color = Green800, radius = 5.dp.toPx(), center = Offset(projX, thresholdY))
                            drawCircle(color = Neutral0, radius = 3.dp.toPx(), center = Offset(projX, thresholdY))
                            val projTextLayout = textMeasurer.measure(projDateLabel, projLabelStyle)
                            drawText(
                                textMeasurer = textMeasurer,
                                text = projDateLabel,
                                style = projLabelStyle,
                                topLeft = Offset(projX - projTextLayout.size.width / 2f, thresholdY - 14.dp.toPx()),
                            )
                        }

                        // Last evaluated day: vertical line and dot
                        drawLine(
                            color = Green800.copy(alpha = 0.35f),
                            start = Offset(lastX, lastY),
                            end = Offset(lastX, yToPx(0.0)),
                            strokeWidth = 1.dp.toPx(),
                        )
                        drawCircle(color = Neutral0, radius = 6.dp.toPx(), center = Offset(lastX, lastY))
                        drawCircle(color = Green800, radius = 4.dp.toPx(), center = Offset(lastX, lastY))

                        // "Hoy · N" badge only while the season is open
                        if (seasonOpen) {
                            val badgeLayout = textMeasurer.measure(todayCalloutText, badgeTextStyle)
                            val badgeWidth = badgeLayout.size.width + 16.dp.toPx()
                            val badgeHeight = badgeLayout.size.height + 6.dp.toPx()
                            val badgeLeft = (lastX - badgeWidth - 8.dp.toPx()).coerceAtLeast(paddingLeft)
                            val badgeTop = lastY - badgeHeight / 2f
                            drawRoundRect(
                                color = Green800,
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
                        }
                    }

                    // X-axis month labels (Jun, Jul, Ago, 31 ago) at their real dates
                    val year = seasonStart.year
                    listOf(
                        LocalDate.of(year, 6, 1) to junLabel,
                        LocalDate.of(year, 7, 1) to julLabel,
                        LocalDate.of(year, 8, 1) to augLabel,
                        LocalDate.of(year, 8, 31) to augEndLabel,
                    ).forEach { (date, label) ->
                        val textLayout = textMeasurer.measure(label, axisTextStyle)
                        // Centered on its date, but kept inside the canvas so "31 ago" never wraps at the edge
                        val left = (xToPx(date) - textLayout.size.width / 2f)
                            .coerceIn(0f, size.width - textLayout.size.width)
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(left, size.height - paddingBottom + 6.dp.toPx()),
                        )
                    }
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
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 2.5.dp)
                .background(color, RoundedCornerShape(1.dp)),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Neutral700,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private fun formatChartDate(date: LocalDate, locale: Locale): String =
    DateTimeFormatter.ofPattern("d MMM", locale).format(date).replace(".", "")
