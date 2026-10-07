package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.core.designsystem.theme.Green700
import pe.edu.upc.viora.core.designsystem.theme.Green800

// The plot drawing of Figma "Ilustración/Lote" (inside "Editorial/Tarjeta de lote"): the real
// outline filled in a pale green, a thin dark border and the trees as small rings in staggered
// rows, optionally over three faint contour lines of the terrain.

/** Fill of the plot inside its outline (Figma #F4F8F5). */
private val PlotFill = Color(0xFFF4F8F5)

/** The faint contour lines behind the plot (Figma #B7C0BA at 70 %). */
private val TerrainLine = Color(0xFFB7C0BA).copy(alpha = 0.7f)

/** Spacing and size of the tree rings; the card uses Figma's values, thumbnails a smaller grid. */
data class TreeGrid(val spacing: Dp, val radius: Dp, val strokeWidth: Dp) {
    companion object {
        val Card = TreeGrid(spacing = 14.dp, radius = 3.2.dp, strokeWidth = 1.2.dp)
        /** The card drawing at the 62 % of Figma's P20 thumbnail. */
        val Thumbnail = TreeGrid(spacing = 8.7.dp, radius = 2.dp, strokeWidth = 0.75.dp)
    }
}

/**
 * Draws the plot [points] (unit square, see [normalizeOutline]) fitted and centred in [area]:
 * fill, trees and border. Nothing is drawn for an unknown outline.
 */
fun DrawScope.drawPlot(points: List<UnitPoint>, area: Rect, trees: TreeGrid?, borderWidth: Dp = 1.6.dp) {
    if (points.size < 3) return
    val side = minOf(area.width, area.height)
    val origin = Offset(area.left + (area.width - side) / 2, area.top + (area.height - side) / 2)
    val corners = points.map { Offset(origin.x + it.x * side, origin.y + it.y * side) }
    val path = Path().apply {
        corners.forEachIndexed { index, corner -> if (index == 0) moveTo(corner.x, corner.y) else lineTo(corner.x, corner.y) }
        close()
    }
    drawPath(path, color = PlotFill)
    if (trees != null) {
        val spacing = trees.spacing.toPx()
        val radius = trees.radius.toPx()
        val ring = Stroke(width = trees.strokeWidth.toPx())
        // A ring is drawn only when it sits fully inside the outline, as an orchard row would.
        val margin = radius + trees.strokeWidth.toPx() + 1.dp.toPx()
        clipPath(path) {
            var row = 0
            var y = origin.y + spacing / 2
            while (y < origin.y + side) {
                var x = origin.x + spacing / 2 + if (row % 2 == 1) spacing / 2 else 0f
                while (x < origin.x + side) {
                    val center = Offset(x, y)
                    if (center.isInside(corners) && corners.distanceToEdges(center) >= margin) {
                        drawCircle(color = Green700, radius = radius, center = center, style = ring)
                    }
                    x += spacing
                }
                y += spacing * ROW_RATIO
                row++
            }
        }
    }
    drawPath(path, color = Green800, style = Stroke(width = borderWidth.toPx(), join = StrokeJoin.Round))
}

/** The three contour lines of the Figma card, scaled from its 272 x 116 frame to this canvas. */
fun DrawScope.drawTerrain() {
    val sx = size.width / FIGMA_WIDTH
    val sy = size.height / FIGMA_HEIGHT
    val stroke = Stroke(width = 1.dp.toPx())
    fun curve(y0: Float, c1: Offset, c2: Offset, mid: Offset, c3: Offset, c4: Offset, end: Offset) = Path().apply {
        moveTo(-10f * sx, y0 * sy)
        cubicTo(c1.x * sx, c1.y * sy, c2.x * sx, c2.y * sy, mid.x * sx, mid.y * sy)
        cubicTo(c3.x * sx, c3.y * sy, c4.x * sx, c4.y * sy, end.x * sx, end.y * sy)
    }
    // Figma "Group" (opacity 0.7): M-10 30 C40 18 80 44 130 30 C180 16 220 10 280 26, etc.
    drawPath(curve(30f, Offset(40f, 18f), Offset(80f, 44f), Offset(130f, 30f), Offset(180f, 16f), Offset(220f, 10f), Offset(280f, 26f)), TerrainLine, style = stroke)
    drawPath(curve(96f, Offset(50f, 84f), Offset(90f, 110f), Offset(150f, 98f), Offset(210f, 86f), Offset(230f, 80f), Offset(280f, 92f)), TerrainLine, style = stroke)
    drawPath(curve(112f, Offset(60f, 100f), Offset(100f, 124f), Offset(170f, 114f), Offset(240f, 104f), Offset(240f, 102f), Offset(280f, 110f)), TerrainLine, style = stroke)
}

/** Ray casting: true when the point is inside the polygon. */
private fun Offset.isInside(polygon: List<Offset>): Boolean {
    var inside = false
    var j = polygon.lastIndex
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[j]
        if ((a.y > y) != (b.y > y) && x < (b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x) inside = !inside
        j = i
    }
    return inside
}

/** Shortest distance from [point] to any edge of the polygon. */
private fun List<Offset>.distanceToEdges(point: Offset): Float =
    indices.minOf { i -> segmentDistance(point, this[i], this[(i + 1) % size]) }

private fun segmentDistance(p: Offset, a: Offset, b: Offset): Float {
    val ab = b - a
    val lengthSquared = ab.x * ab.x + ab.y * ab.y
    if (lengthSquared == 0f) return (p - a).getDistance()
    val t = (((p.x - a.x) * ab.x + (p.y - a.y) * ab.y) / lengthSquared).coerceIn(0f, 1f)
    return (p - Offset(a.x + ab.x * t, a.y + ab.y * t)).getDistance()
}

/** Rows a bit closer than columns, as the staggered rows of the Figma drawing. */
private const val ROW_RATIO = 0.9f
private const val FIGMA_WIDTH = 272f
private const val FIGMA_HEIGHT = 116f
