package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.phenology.presentation.ui.formatKg
import pe.edu.upc.viora.features.phenology.presentation.ui.formatPercent
import pe.edu.upc.viora.features.phenology.presentation.ui.formatTonnesPerHectare

/** P72, "¿Asentar la cosecha 2026?": what will be closed and the total, with a loading confirm button. */
@Composable
internal fun ConfirmSettleDialog(
    year: Int,
    plotName: String,
    totalKg: Double,
    tonnesPerHectare: Double?,
    greenShare: Double?,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onReview: () -> Unit,
) {
    val green = greenShare?.let { formatPercent(it) }
    val caption = when {
        tonnesPerHectare != null && green != null -> stringResource(R.string.settle_confirm_caption_tonnes, formatTonnesPerHectare(tonnesPerHectare), green)
        green != null -> stringResource(R.string.settle_confirm_caption_green, green)
        else -> null
    }
    SettleDialogFrame(
        icon = R.drawable.ic_inventory,
        iconBackground = Green200,
        iconTint = Green900,
        lead = stringResource(R.string.settle_confirm_lead),
        emphasis = stringResource(R.string.settle_confirm_emphasis, year),
        body = stringResource(R.string.settle_confirm_body, year, plotName),
        summaryValue = stringResource(R.string.settle_total_value, formatKg(totalKg)),
        summaryCaption = caption,
        primaryText = stringResource(R.string.settle_submit),
        primaryLoading = isSaving,
        onPrimary = onConfirm,
        secondaryText = stringResource(R.string.settle_confirm_review),
        secondaryEnabled = !isSaving,
        onSecondary = onReview,
        onDismiss = { if (!isSaving) onReview() },
    )
}

/**
 * P72 (409), "Esta campaña ya está cerrada". [existing] may be null, and its receipt number and
 * date may be missing (older backend), so the copy and the summary degrade without them.
 */
@Composable
internal fun AlreadySettledDialog(
    year: Int,
    plotName: String,
    existing: SettlementSummary?,
    onViewReceipt: () -> Unit,
    onDismiss: () -> Unit,
) {
    val date = existing?.weighedOn
    val dayText = date?.let { formatSettleDate(it, R.string.settle_pattern_day) }
    val shortText = date?.let { formatSettleDate(it, R.string.settle_pattern_short) }
    val receipt = existing?.receiptNumber?.takeIf { it.isNotBlank() }
    val caption = when {
        receipt != null && shortText != null -> stringResource(R.string.settle_conflict_caption_both, receipt, shortText)
        receipt != null -> stringResource(R.string.settle_conflict_caption_receipt, receipt)
        else -> shortText
    }
    SettleDialogFrame(
        icon = R.drawable.ic_info,
        iconBackground = Harvest100,
        iconTint = Harvest800,
        lead = stringResource(R.string.settle_conflict_lead),
        emphasis = stringResource(R.string.settle_conflict_emphasis),
        body = if (dayText != null) {
            stringResource(R.string.settle_conflict_body_dated, year, plotName, dayText)
        } else {
            stringResource(R.string.settle_conflict_body, year, plotName)
        },
        summaryValue = existing?.let { stringResource(R.string.settle_total_value, formatKg(it.totalYieldKg)) },
        summaryCaption = caption,
        primaryText = stringResource(R.string.settle_conflict_view),
        primaryLoading = false,
        onPrimary = onViewReceipt,
        secondaryText = stringResource(R.string.settle_conflict_ok),
        secondaryEnabled = true,
        onSecondary = onDismiss,
        onDismiss = onDismiss,
    )
}

@Composable
private fun SettleDialogFrame(
    @DrawableRes icon: Int,
    iconBackground: Color,
    iconTint: Color,
    lead: String,
    emphasis: String,
    body: String,
    summaryValue: String?,
    summaryCaption: String?,
    primaryText: String,
    primaryLoading: Boolean,
    onPrimary: () -> Unit,
    secondaryText: String,
    secondaryEnabled: Boolean,
    onSecondary: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(iconBackground), contentAlignment = Alignment.Center) {
                Icon(painterResource(icon), contentDescription = null, tint = iconTint)
            }
            Column {
                Text(lead, style = MaterialTheme.typography.headlineLarge.copy(lineHeight = 32.sp), color = Neutral900)
                Text(emphasis, style = MaterialTheme.typography.headlineLarge.copy(lineHeight = 32.sp), fontStyle = FontStyle.Italic, color = Neutral900)
            }
            Text(body, style = MaterialTheme.typography.bodyMedium, color = Neutral700)
            if (summaryValue != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(summaryValue, style = MaterialTheme.typography.headlineMedium, color = Neutral900)
                    if (summaryCaption != null) Text(summaryCaption, style = MaterialTheme.typography.bodySmall, color = Neutral600)
                }
            }
            Button(
                onClick = onPrimary,
                enabled = !primaryLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Green900, contentColor = Neutral0, disabledContainerColor = Green900, disabledContentColor = Neutral0),
            ) {
                if (primaryLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Neutral0)
                } else {
                    Text(primaryText, style = MaterialTheme.typography.titleMedium)
                }
            }
            TextButton(onClick = onSecondary, enabled = secondaryEnabled, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(secondaryText, style = MaterialTheme.typography.titleMedium, color = Neutral900)
            }
        }
    }
}
