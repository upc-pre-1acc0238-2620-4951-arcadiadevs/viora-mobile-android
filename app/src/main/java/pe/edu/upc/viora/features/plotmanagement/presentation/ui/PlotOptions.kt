package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.presentation.state.ArchiveState
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * The options of a plot (Figma P27): a sheet with the plot's name, its summary and a white card of
 * rows: edit its data, adjust its outline, its sensors and archive it. Its sections (alternation,
 * plan, climate...) are reached from the detail and from [LotSectionsSheet], not from here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotOptionsSheet(
    plot: Plot,
    onEdit: () -> Unit,
    onAdjustOutline: () -> Unit,
    onSensors: () -> Unit,
    onArchive: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(Neutral300)) },
    ) {
        Column(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = plot.name,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.weight(1f).padding(end = 56.dp),
                )
                CircleIconButton(
                    icon = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.action_close),
                    onClick = onDismiss,
                )
            }
            Text(
                text = stringResource(
                    R.string.plot_options_subtitle,
                    stringResource(plot.variety.labelRes()),
                    formatHectares(plot.areaHectares),
                    formatCount(plot.estimatedTrees),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
            )
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(horizontal = 16.dp),
            ) {
                OptionRow(
                    icon = R.drawable.ic_edit_note,
                    title = stringResource(R.string.plot_menu_edit),
                    hint = stringResource(R.string.plot_menu_edit_hint),
                    onClick = onEdit,
                )
                OptionDivider()
                OptionRow(
                    icon = R.drawable.ic_map,
                    title = stringResource(R.string.plot_menu_outline),
                    hint = stringResource(R.string.plot_menu_outline_hint),
                    onClick = onAdjustOutline,
                )
                OptionDivider()
                OptionRow(
                    icon = R.drawable.ic_sensors,
                    title = stringResource(R.string.plot_menu_sensors),
                    hint = stringResource(R.string.plot_menu_sensors_hint),
                    onClick = onSensors,
                )
                OptionDivider()
                OptionRow(
                    icon = R.drawable.ic_history,
                    title = stringResource(R.string.plot_menu_archive),
                    hint = stringResource(R.string.plot_menu_archive_hint, formatHectares(plot.areaHectares)),
                    onClick = onArchive,
                    isDestructive = true,
                )
            }
        }
    }
}

@Composable
internal fun OptionDivider() {
    HorizontalDivider(color = Neutral200)
}

@Composable
internal fun OptionRow(
    @DrawableRes icon: Int,
    title: String,
    hint: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isDestructive) Terracotta100 else Green200),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (isDestructive) Terracotta700 else Neutral900,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isDestructive) Terracotta700 else Neutral900,
            )
            Text(text = hint, style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
        if (!isDestructive && enabled) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Neutral600,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Asks for confirmation before archiving a plot (Figma P28): a white card over a dark veil, with
 * how many hectares the producer keeps in active plots before and after.
 */
@Composable
fun ArchivePlotDialog(
    plotName: String,
    activeHectaresBefore: Double,
    activeHectaresAfter: Double,
    state: ArchiveState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (state != ArchiveState.Working) onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Neutral0).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Green200),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = painterResource(R.drawable.ic_history), contentDescription = null, tint = Neutral900)
            }
            Column {
                val titleStyle = MaterialTheme.typography.headlineMedium
                Text(text = stringResource(R.string.archive_title_lead), style = titleStyle)
                Text(
                    text = stringResource(R.string.archive_title_name, plotName),
                    style = titleStyle,
                    fontStyle = FontStyle.Italic,
                )
            }
            Text(text = stringResource(R.string.archive_body), style = MaterialTheme.typography.bodyMedium, color = Neutral900)
            ActiveHectaresChange(before = activeHectaresBefore, after = activeHectaresAfter)
            if (state == ArchiveState.Failed) {
                Text(
                    text = stringResource(R.string.archive_failed),
                    style = MaterialTheme.typography.bodySmall,
                    color = Terracotta700,
                )
            }
            PrimaryPillButton(
                text = stringResource(R.string.archive_confirm),
                onClick = onConfirm,
                isLoading = state == ArchiveState.Working,
                modifier = Modifier.padding(top = 4.dp),
            )
            TextButton(
                onClick = onDismiss,
                enabled = state != ArchiveState.Working,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.archive_cancel), style = MaterialTheme.typography.titleSmall, color = Neutral900)
            }
        }
    }
}

/** "4,0 → 1,5 ha in your active plots": the serif figures of the dialog's usage line. */
@Composable
private fun ActiveHectaresChange(before: Double, after: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatHectares(before),
            style = MaterialTheme.typography.headlineSmall,
            color = Neutral600,
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = Neutral900,
            modifier = Modifier.size(16.dp),
        )
        Text(text = formatHectares(after), style = MaterialTheme.typography.headlineSmall, color = Neutral900)
        Text(
            text = stringResource(R.string.archive_usage_caption),
            style = MaterialTheme.typography.bodySmall,
            color = Neutral600,
            modifier = Modifier.weight(1f),
        )
    }
}
