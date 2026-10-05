package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsFilter

/**
 * Displays 3 vertical test-tube pills with liquid fill levels and count metrics.
 */
@Composable
fun AlertFilterCapsules(
    summary: AlertsSummary,
    activeFilter: AlertsFilter,
    onFilterSelected: (AlertsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxCount = maxOf(summary.criticalCount, summary.warningCount, summary.normalizedCount)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(vertical = 20.dp, horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterCapsuleItem(
                count = summary.criticalCount,
                maxCount = maxCount,
                label = stringResource(R.string.alerts_filter_critical),
                accentColor = Terracotta500,
                numberColor = Neutral0,
                isSelected = activeFilter == AlertsFilter.CRITICAL,
                onClick = { onFilterSelected(AlertsFilter.CRITICAL) },
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(12.dp))

            FilterCapsuleItem(
                count = summary.warningCount,
                maxCount = maxCount,
                label = stringResource(R.string.alerts_filter_warning),
                accentColor = Harvest300,
                numberColor = Neutral900,
                isSelected = activeFilter == AlertsFilter.WARNING,
                onClick = { onFilterSelected(AlertsFilter.WARNING) },
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(12.dp))

            FilterCapsuleItem(
                count = summary.normalizedCount,
                maxCount = maxCount,
                label = stringResource(R.string.alerts_filter_normalized),
                accentColor = Green800,
                numberColor = Neutral0,
                isSelected = activeFilter == AlertsFilter.NORMALIZED,
                onClick = { onFilterSelected(AlertsFilter.NORMALIZED) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FilterCapsuleItem(
    count: Long,
    maxCount: Long,
    label: String,
    accentColor: Color,
    numberColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderWidth by animateDpAsState(targetValue = if (isSelected) 2.5.dp else 0.dp, label = "capsule_border")
    val borderColor by animateColorAsState(targetValue = if (isSelected) accentColor else Color.Transparent, label = "capsule_border_color")

    val pillShape = RoundedCornerShape(42.dp)

    // Calculate proportional liquid height:
    // If count == maxCount (> 0), it is always FULL (150.dp).
    // For other counts, it scales proportionally between min (56.dp) and max (150.dp).
    val minLiquidHeight = 56.dp
    val maxLiquidHeight = 150.dp
    val liquidHeight = when {
        count <= 0L -> minLiquidHeight
        maxCount <= 0L || count >= maxCount -> maxLiquidHeight
        else -> {
            val fraction = count.toFloat() / maxCount.toFloat()
            (maxLiquidHeight * fraction).coerceIn(minLiquidHeight, maxLiquidHeight)
        }
    }
    val animatedLiquidHeight by animateDpAsState(targetValue = liquidHeight, label = "capsule_liquid_height")

    val effectiveAccent = if (count <= 0L) Neutral200 else accentColor
    val effectiveNumberColor = if (count <= 0L) Neutral600 else numberColor

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Vertical capsule pill
        Box(
            modifier = Modifier
                .width(84.dp)
                .height(150.dp)
                .clip(pillShape)
                .border(borderWidth, borderColor, pillShape)
                .background(Neutral100),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // Liquid fill block at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(animatedLiquidHeight)
                    .clip(pillShape)
                    .background(effectiveAccent),
            )

            // Number text placed at fixed bottom position, horizontally aligned across all capsules (Figma T14)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    color = effectiveNumberColor,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = label,
            fontFamily = RobotoFamily,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 13.sp,
            color = Neutral900,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
