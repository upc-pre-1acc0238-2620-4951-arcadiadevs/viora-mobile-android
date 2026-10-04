package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

/**
 * A plot in the list: silhouette, name and summary, and optionally a [badge] under them. Tapping it
 * opens the plot's detail; with a null [onClick] the card only shows the plot.
 */
@Composable
fun PlotCard(plot: Plot, onClick: (() -> Unit)?, modifier: Modifier = Modifier, badge: String? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlotSilhouette(outline = plot.outline)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
            if (badge != null) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelMedium,
                    color = Neutral900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        if (onClick != null) {
            Icon(painter = painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Neutral600)
        }
    }
}

/** "Sevillana · 2,5 ha · 180 árboles", localised. */
@Composable
fun plotSummary(plot: Plot): String {
    val variety = stringResource(plot.variety.labelRes())
    val area = formatHectares(plot.areaHectares)
    val trees = pluralStringResource(
        R.plurals.plots_trees_count,
        plot.estimatedTrees,
        formatCount(plot.estimatedTrees),
    )
    return stringResource(R.string.plots_summary, variety, area, trees)
}
