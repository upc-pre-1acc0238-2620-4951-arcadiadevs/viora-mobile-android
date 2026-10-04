package pe.edu.upc.viora.features.home.presentation.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatCount
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes

/**
 * Picks the plot the Home talks about (the context chip opens it): the producer's plots with the
 * one in focus marked, under a line with how many plots and hectares they have in total.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFocusSheet(
    plots: List<Plot>,
    focusedId: PlotId?,
    onSelect: (PlotId) -> Unit,
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
            Text(text = stringResource(R.string.home_focus_title), style = MaterialTheme.typography.displaySmall, color = Neutral900)
            Text(
                text = pluralStringResource(R.plurals.home_plots_chip, plots.size, plots.size, formatHectares(plots.sumOf { it.areaHectares })) +
                    " · " + stringResource(R.string.home_focus_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
            )
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(horizontal = 16.dp)) {
                plots.forEachIndexed { position, plot ->
                    FocusRow(plot = plot, selected = plot.id == focusedId, onClick = { onSelect(plot.id) })
                    if (position < plots.lastIndex) HorizontalDivider(color = Neutral200)
                }
            }
        }
    }
}

@Composable
private fun FocusRow(plot: Plot, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = plot.name, style = MaterialTheme.typography.titleSmall, color = Neutral900)
            Text(
                text = stringResource(
                    R.string.plot_options_subtitle,
                    stringResource(plot.variety.labelRes()),
                    formatHectares(plot.areaHectares),
                    formatCount(plot.estimatedTrees),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
        if (selected) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Green800), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Neutral0, modifier = Modifier.size(18.dp))
            }
        }
    }
}
