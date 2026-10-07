package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.presentation.state.PlotFilterOption

/**
 * "Filtrar por lote" sheet of the Alerts Center (Figma "Capa · Filtrar por lote / abierta").
 * Every plot with alerts, with the dot of its worst active alert and how many it has; the check marks
 * the choice. A single choice, so a tap applies it at once (the screen updates behind) and the sheet
 * closes after the check moves; the X or a swipe down closes it without changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsPlotFilterSheet(
    options: List<PlotFilterOption>,
    selectedPlotId: String?,
    totalActive: Int,
    onApply: (plotId: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var chosen by rememberSaveable { mutableStateOf(selectedPlotId) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val choose: (String?) -> Unit = { plotId ->
        chosen = plotId
        onApply(plotId)
        scope.launch {
            delay(CHECK_VISIBLE_MS)
            sheetState.hide()
        }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(Neutral300)) },
    ) {
        Column(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp).padding(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val title = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp, lineHeight = 38.sp)
                    Column {
                        Text(text = stringResource(R.string.alerts_plot_filter_title_lead), style = title, color = Neutral900)
                        Text(text = stringResource(R.string.alerts_plot_filter_title_emphasis), style = title, fontStyle = FontStyle.Italic, color = Neutral900)
                    }
                    Text(text = stringResource(R.string.alerts_plot_filter_hint), style = MaterialTheme.typography.bodySmall, color = Neutral600)
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Neutral0).clickable(role = Role.Button, onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.alerts_plot_filter_close), tint = Neutral900)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0)) {
                PlotRow(
                    name = stringResource(R.string.alerts_plot_filter_all),
                    detail = pluralStringResource(R.plurals.alerts_plot_filter_active, totalActive, totalActive),
                    dot = null,
                    selected = chosen == null,
                    onClick = { choose(null) },
                )
                options.forEach { option ->
                    HorizontalDivider(color = Neutral200, modifier = Modifier.padding(start = 48.dp))
                    PlotRow(
                        name = option.plotName,
                        detail = detailOf(option),
                        dot = dotColorOf(option.worstSeverity),
                        selected = chosen == option.plotId,
                        onClick = { choose(option.plotId) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.alerts_plot_filter_note),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

/** How long the check stays on the tapped row before the sheet slides away. */
private const val CHECK_VISIBLE_MS = 150L

/** "1 crítica", "2 de atención", "3 alertas activas" when both kinds mix, or "Sin alertas activas". */
@Composable
private fun detailOf(option: PlotFilterOption): String = when {
    option.activeCount == 0 -> stringResource(R.string.alerts_plot_filter_no_active)
    option.warningCount == 0 -> pluralStringResource(R.plurals.alerts_plot_filter_critical, option.criticalCount, option.criticalCount)
    option.criticalCount == 0 -> pluralStringResource(R.plurals.alerts_plot_filter_warning, option.warningCount, option.warningCount)
    else -> pluralStringResource(R.plurals.alerts_plot_filter_active, option.activeCount, option.activeCount)
}

/** The same colors as the capsules; a plot with only normalized alerts gets a quiet grey dot. */
private fun dotColorOf(severity: IncidentSeverity?): Color = when (severity) {
    IncidentSeverity.CRITICAL -> Terracotta500
    IncidentSeverity.WARNING -> Harvest300
    else -> Neutral300
}

/** One row of the list: the dot (a hollow ring for every plot), name and count, and the check when chosen. */
@Composable
private fun PlotRow(name: String, detail: String, dot: Color?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val dotModifier = Modifier.padding(horizontal = 3.dp).size(10.dp).clip(CircleShape)
        Box(if (dot == null) dotModifier.border(1.5.dp, Neutral900, CircleShape) else dotModifier.background(dot))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleSmall, color = Neutral900)
            Text(text = detail, style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
        if (selected) {
            Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Green800, modifier = Modifier.size(24.dp))
        }
    }
}
