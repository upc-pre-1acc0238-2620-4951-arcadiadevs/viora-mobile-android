package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

@Composable
fun PlotCard(plot: Plot, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlotSilhouette(outline = plot.outline)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = plot.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, letterSpacing = (-0.22).sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = plotSummary(plot),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
                color = Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** "Sevillana · 2,5 ha · 180 árboles", localised. */
@Composable
private fun plotSummary(plot: Plot): String {
    val variety = stringResource(plot.variety.labelRes())
    val area = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 2
    }.format(plot.areaHectares)
    val trees = pluralStringResource(
        R.plurals.plots_trees_count,
        plot.estimatedTrees,
        NumberFormat.getIntegerInstance().format(plot.estimatedTrees),
    )
    return stringResource(R.string.plots_summary, variety, area, trees)
}

private fun OliveVariety.labelRes(): Int = when (this) {
    OliveVariety.CRIOLLA -> R.string.variety_criolla
    OliveVariety.SEVILLANA -> R.string.variety_sevillana
    OliveVariety.MANZANILLA -> R.string.variety_manzanilla
    OliveVariety.ARBEQUINA -> R.string.variety_arbequina
}
