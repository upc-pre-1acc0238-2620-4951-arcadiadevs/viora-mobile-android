package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.HarvestEntryUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.PlotToSettle
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton

/**
 * P70, "¿Qué lote cosechaste?": the plots still "por registrar" in the campaign. Choosing one
 * opens the settle form for it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestPlotPickerSheet(
    state: HarvestEntryUiState,
    onSelect: (PlotToSettle) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(Neutral300)) },
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val title = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
                    Column {
                        Text(stringResource(R.string.settle_picker_title_lead), style = title, color = Neutral900)
                        Text(stringResource(R.string.settle_picker_title_emphasis), style = title, fontStyle = FontStyle.Italic, color = Neutral900)
                    }
                    Text(
                        text = pluralStringResource(R.plurals.settle_picker_subtitle, state.pendingCount, state.campaignYear, state.pendingCount),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = Neutral600,
                    )
                }
                CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onDismiss)
            }
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.plots.forEach { plot -> PlotRow(plot = plot, onClick = { onSelect(plot) }) }
            }
            Text(stringResource(R.string.settle_picker_note), style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
    }
}

@Composable
private fun PlotRow(plot: PlotToSettle, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Green200), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_grass), contentDescription = null, tint = Green900)
        }
        Column(Modifier.weight(1f)) {
            Text(plot.name, style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp), color = Neutral900, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = stringResource(R.string.settle_plot_subtitle, stringResource(plot.variety.labelRes()), formatHectares(plot.hectares)),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
        Row(
            modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Harvest100).padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Harvest400))
            Text(stringResource(R.string.settle_picker_status), style = MaterialTheme.typography.labelSmall, color = Harvest800)
        }
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Neutral600, modifier = Modifier.size(20.dp))
    }
}
