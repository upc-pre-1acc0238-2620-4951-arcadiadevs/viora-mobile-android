package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.telemetry.domain.entity.WeeklyTrendPoint
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity

/**
 * 7-day trend chart matching Figma T15 ("Máximas · cápsulas").
 * Displays rounded vertical test-tubes (36dp x 176dp) filled with liquid
 * proportionally to daily peak values, with a dashed threshold line.
 */
@Composable
fun WeeklyTrendChart(
    weeklyTrend: List<WeeklyTrendPoint>,
    thresholdValue: Double,
    unit: String,
    severity: IncidentSeverity,
    modifier: Modifier = Modifier,
) {
    val isCritical = severity == IncidentSeverity.CRITICAL
    val cardBg = if (isCritical) Terracotta100 else Harvest100
    val accentColor = if (isCritical) Terracotta500 else Harvest400
    val thresholdTextColor = if (isCritical) Terracotta700 else Harvest800
    val thresholdLineColor = if (isCritical) Terracotta600 else Harvest800
    val peakValueColor = if (isCritical) Terracotta700 else Harvest800

    val locale = LocalConfiguration.current.locales[0]

    val peakPoint = weeklyTrend.maxByOrNull { it.value }
    val peakDisplay = formatMetricValue(locale, peakPoint?.value ?: thresholdValue, unit)

    val tubeHeight = 176.dp
    val tubeShape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBg)
            .padding(vertical = 22.dp, horizontal = 20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Title on left, peak value on right (Figma T15)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.alert_chart_title),
                    fontFamily = RobotoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = peakValueColor,
                )

                Text(
                    text = peakDisplay,
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                    color = peakValueColor,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (weeklyTrend.isNotEmpty()) {
                val allValues = weeklyTrend.map { it.value } + thresholdValue
                val rawMin = allValues.minOrNull() ?: 0.0
                val rawMax = allValues.maxOrNull() ?: thresholdValue
                val spread = (rawMax - rawMin).coerceAtLeast(1.0)

                // Map min value to 70.dp and max value to 156.dp inside the 176.dp tube
                val minDisplayHeight = 70.dp
                val maxDisplayHeight = 156.dp
                val heightRange = maxDisplayHeight - minDisplayHeight

                val thresholdFraction = ((thresholdValue - rawMin) / spread).toFloat().coerceIn(0f, 1f)
                val thresholdHeightFromBottom = minDisplayHeight + heightRange * thresholdFraction

                val density = LocalDensity.current

                // Container for test-tubes and threshold line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawWithContent {
                            drawContent()
                            val thresholdHeightPx = with(density) { thresholdHeightFromBottom.toPx() }
                            // Line Y is relative to the tube height (tubes occupy the upper 176.dp of the day column)
                            val lineY = with(density) { tubeHeight.toPx() } - thresholdHeightPx
                            drawLine(
                                color = thresholdLineColor,
                                start = Offset(0f, lineY),
                                end = Offset(size.width, lineY),
                                strokeWidth = 1.2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f),
                            )
                        },
                ) {
                    // 7 days columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        weeklyTrend.forEach { point ->
                            val exceedsThreshold = point.value >= thresholdValue
                            val fraction = ((point.value - rawMin) / spread).toFloat().coerceIn(0f, 1f)
                            val targetLiquidHeight = minDisplayHeight + heightRange * fraction
                            val animatedLiquidHeight by animateDpAsState(
                                targetValue = targetLiquidHeight,
                                label = "tube_liquid_height",
                            )

                            val dayLabel = runCatching {
                                val instant = Instant.parse(point.timestamp)
                                instant.atZone(ZoneId.systemDefault())
                                    .dayOfWeek
                                    .getDisplayName(TextStyle.SHORT, locale)
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                            }.getOrDefault("")

                            val valueFormatted = if (point.value % 1.0 == 0.0) {
                                "${point.value.toInt()}°"
                            } else {
                                "${point.value}°"
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // Rounded test-tube capsule (Figma: width 36dp, height 176dp, radius 18dp)
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(tubeHeight)
                                        .clip(tubeShape)
                                        .background(Neutral0.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.BottomCenter,
                                ) {
                                    // Liquid fill rising from bottom
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(animatedLiquidHeight)
                                            .clip(tubeShape)
                                            .background(if (exceedsThreshold) accentColor else Neutral0),
                                        contentAlignment = Alignment.TopCenter,
                                    ) {
                                        // Peak reading text inside liquid at top
                                        Text(
                                            text = valueFormatted,
                                            fontFamily = RobotoFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.sp,
                                            color = if (exceedsThreshold) Neutral50 else Neutral900,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .padding(top = 10.dp)
                                                .wrapContentSize(Alignment.Center),
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Day label under tube (e.g., "Mar", "Mié", "Jue")
                                Text(
                                    text = dayLabel,
                                    fontFamily = RobotoFamily,
                                    fontWeight = if (exceedsThreshold) FontWeight.Medium else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = if (exceedsThreshold) peakValueColor else Neutral700,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.wrapContentSize(Alignment.Center),
                                )
                            }
                        }
                    }

                    // "umbral 32°" label placed just above the dashed line on the left, rendered in front of test-tubes
                    Text(
                        text = stringResource(R.string.alert_chart_threshold_label, formatMetricValue(locale, thresholdValue, unit)),
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = thresholdTextColor,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = 0.dp,
                                y = tubeHeight - thresholdHeightFromBottom - 16.dp,
                            ),
                    )
                }
            }
        }
    }
}
