package pe.edu.upc.viora.features.home.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.ShadowTint
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.TreeGrid
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.drawPlot
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.drawTerrain
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.normalizeOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.plotSummary

/** Horizontal strip of plot cards under "Mis lotes". Bleeds to the screen edges. */
@Composable
fun HomePlotsCarousel(
    plots: List<Plot>,
    onOpenPlot: (PlotId) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp),
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(plots, key = { it.id.value }) { plot ->
            HomePlotCard(plot = plot, onClick = { onOpenPlot(plot.id) }, modifier = Modifier.padding(vertical = 16.dp))
        }
    }
}

/**
 * A plot card of the Home (Figma "Editorial/Tarjeta de lote"): the real outline on a soft tile,
 * the load status chip, name and summary. The load status and the alternation row need the
 * phenology data (US20) and are not shown yet, so the chip says there is not enough data.
 */
@Composable
fun HomePlotCard(plot: Plot, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .width(272.dp)
            .shadow(elevation = 8.dp, shape = shape, ambientColor = ShadowTint, spotColor = ShadowTint)
            .clip(shape)
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(116.dp).background(Green100)) {
            OutlineArt(plot = plot, modifier = Modifier.fillMaxSize())
            StatusChip(
                text = stringResource(R.string.home_plot_status_no_data),
                modifier = Modifier.align(Alignment.TopStart).padding(start = 14.dp, top = 14.dp),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = plot.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.22).sp),
                color = Neutral900,
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

@Composable
private fun StatusChip(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = CircleShape, ambientColor = ShadowTint, spotColor = ShadowTint)
            .clip(CircleShape)
            .background(Neutral0)
            .padding(start = 10.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Neutral300))
        Text(text = text, style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp), color = Neutral700, maxLines = 1)
    }
}

/**
 * The plot's real polygon on the card's tile, drawn as Figma "Ilustración/Lote": terrain lines,
 * the outline with its trees, placed right of centre so the status chip has room on the left.
 */
@Composable
private fun OutlineArt(plot: Plot, modifier: Modifier = Modifier) {
    val points = remember(plot.outline) { normalizeOutline(plot.outline) }
    Canvas(modifier = modifier) {
        drawTerrain()
        // Figma: the plot spans x 100–240 and y 16–102 of the 272 x 116 tile.
        drawPlot(
            points = points,
            area = Rect(left = size.width * 0.34f, top = 14.dp.toPx(), right = size.width * 0.93f, bottom = size.height - 12.dp.toPx()),
            trees = TreeGrid.Card,
        )
    }
}
