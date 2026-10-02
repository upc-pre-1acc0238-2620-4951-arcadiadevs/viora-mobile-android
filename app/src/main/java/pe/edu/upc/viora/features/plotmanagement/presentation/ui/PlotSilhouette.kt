package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

/** The plot's real outline drawn inside a soft green tile; blank when the outline is unknown. */
@Composable
fun PlotSilhouette(outline: List<GeoPoint>, modifier: Modifier = Modifier) {
    val points = remember(outline) { normalizeOutline(outline) }
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Green100),
    ) {
        if (points.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                val path = Path().apply {
                    points.forEachIndexed { index, point ->
                        val position = Offset(point.x * size.width, point.y * size.height)
                        if (index == 0) moveTo(position.x, position.y) else lineTo(position.x, position.y)
                    }
                    close()
                }
                drawPath(path, color = Green200)
                drawPath(path, color = Green800, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))
            }
        }
    }
}
