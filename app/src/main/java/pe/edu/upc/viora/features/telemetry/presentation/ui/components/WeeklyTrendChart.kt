package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.telemetry.domain.entity.WeeklyTrendPoint
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity

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
    val thresholdTextColor = if (isCritical) Terracotta600 else Harvest800
    val peakValueColor = if (isCritical) Terracotta700 else Harvest800

    val peakPoint = weeklyTrend.maxByOrNull { it.value }
    val peakDisplay = peakPoint?.let {
        if (it.value % 1.0 == 0.0) "${it.value.toInt()}$unit" else "${it.value} $unit"
    } ?: "${thresholdValue.toInt()}$unit"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBg)
            .padding(20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.alert_chart_title),
                        fontFamily = NewsreaderFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Neutral900,
                    )
                    Text(
                        text = stringResource(R.string.alert_chart_threshold_label, thresholdValue.toInt(), unit),
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = thresholdTextColor,
                    )
                }

                Text(
                    text = peakDisplay,
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = peakValueColor,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (weeklyTrend.isNotEmpty()) {
                val maxVal = max(thresholdValue, weeklyTrend.maxOfOrNull { it.value } ?: thresholdValue) * 1.15
                val minVal = max(0.0, (weeklyTrend.minOfOrNull { it.value } ?: 0.0) * 0.7)
                val valueRange = (maxVal - minVal).coerceAtLeast(1.0)

                val thresholdFraction = ((thresholdValue - minVal) / valueRange).toFloat().coerceIn(0.1f, 0.9f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .drawBehind {
                            val y = size.height * (1f - thresholdFraction)
                            drawLine(
                                color = thresholdTextColor.copy(alpha = 0.7f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                            )
                        },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        weeklyTrend.forEach { point ->
                            val exceedsThreshold = point.value >= thresholdValue
                            val heightFraction = ((point.value - minVal) / valueRange).toFloat().coerceIn(0.12f, 1f)

                            val dayLabel = runCatching {
                                val instant = Instant.parse(point.timestamp)
                                instant.atZone(ZoneId.systemDefault())
                                    .dayOfWeek
                                    .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            }.getOrDefault("")

                            val valueFormatted = if (point.value % 1.0 == 0.0) {
                                "${point.value.toInt()}°"
                            } else {
                                "${point.value}°"
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                            ) {
                                Text(
                                    text = valueFormatted,
                                    fontFamily = RobotoFamily,
                                    fontWeight = if (exceedsThreshold) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = if (exceedsThreshold) accentColor else Neutral600,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .fillMaxHeight(heightFraction * 0.78f)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(
                                            if (exceedsThreshold) accentColor else Neutral0.copy(alpha = 0.65f),
                                        ),
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = dayLabel,
                                    fontFamily = RobotoFamily,
                                    fontWeight = if (exceedsThreshold) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (exceedsThreshold) Neutral900 else Neutral600,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
