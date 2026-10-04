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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsFilter

@Composable
fun AlertFilterCapsules(
    summary: AlertsSummary,
    activeFilter: AlertsFilter,
    onFilterSelected: (AlertsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(vertical = 18.dp, horizontal = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FilterCapsuleItem(
                count = summary.criticalCount,
                label = stringResource(R.string.alerts_filter_critical),
                accentColor = Terracotta500,
                accentBgColor = Terracotta100,
                numberColor = Terracotta700,
                isSelected = activeFilter == AlertsFilter.CRITICAL,
                onClick = { onFilterSelected(AlertsFilter.CRITICAL) },
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(10.dp))

            FilterCapsuleItem(
                count = summary.warningCount,
                label = stringResource(R.string.alerts_filter_warning),
                accentColor = Harvest300,
                accentBgColor = Harvest100,
                numberColor = Harvest800,
                isSelected = activeFilter == AlertsFilter.WARNING,
                onClick = { onFilterSelected(AlertsFilter.WARNING) },
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(10.dp))

            FilterCapsuleItem(
                count = summary.normalizedCount,
                label = stringResource(R.string.alerts_filter_normalized),
                accentColor = Green800,
                accentBgColor = Green100,
                numberColor = Green800,
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
    label: String,
    accentColor: Color,
    accentBgColor: Color,
    numberColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderWidth by animateDpAsState(targetValue = if (isSelected) 2.dp else 0.dp, label = "capsule_border")
    val borderColor by animateColorAsState(targetValue = if (isSelected) accentColor else Color.Transparent, label = "capsule_border_color")
    val containerBg = if (isSelected) accentBgColor.copy(alpha = 0.5f) else Neutral100

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(22.dp))
            .background(containerBg)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(accentColor),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = count.toString(),
            fontFamily = NewsreaderFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            color = numberColor,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = RobotoFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            color = if (isSelected) Neutral900 else Neutral600,
        )
    }
}
