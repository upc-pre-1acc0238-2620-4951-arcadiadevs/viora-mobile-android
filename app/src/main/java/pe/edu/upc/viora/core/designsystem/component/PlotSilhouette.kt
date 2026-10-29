package pe.edu.upc.viora.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.domain.GeoPoint

/**
 * The plot thumbnail of Figma P20 "Silueta": the same drawing as the Home plot card (terrain,
 * outline and trees, see PlotArt) at 62 % inside a 72 dp tile; the tile alone when the outline
 * is unknown. [showTreeGrid] = false leaves the trees out.
 */
@Composable
fun PlotSilhouette(
    outline: List<GeoPoint>,
    modifier: Modifier = Modifier,
    showTreeGrid: Boolean = true,
) {
    val points = remember(outline) { normalizeOutline(outline) }
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Green100),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTerrain()
            val inset = 10.dp.toPx()
            drawPlot(
                points = points,
                area = Rect(inset, inset, size.width - inset, size.height - inset),
                trees = if (showTreeGrid) TreeGrid.Thumbnail else null,
                borderWidth = 1.dp,
            )
        }
    }
}
