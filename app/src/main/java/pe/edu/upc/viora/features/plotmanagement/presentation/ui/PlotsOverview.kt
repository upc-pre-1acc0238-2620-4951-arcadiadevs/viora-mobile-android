package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

private const val MAX_LABELS = 5
private val PADDING = 24.dp
private const val METERS_PER_DEGREE_LATITUDE = 110_574.0
private const val METERS_PER_DEGREE_LONGITUDE_AT_EQUATOR = 111_320.0

/** Several outlines laid out in one flat drawing: points in metres (x east, y south) from the top-left corner. */
data class ProjectedOutlines(val shapes: List<List<Offset>>, val width: Float, val height: Float)

/**
 * Flattens [outlines] into a single drawing that keeps their relative position and size, so
 * the plots look as they sit next to each other. Empty outlines give an empty drawing.
 */
fun projectOutlines(outlines: List<List<GeoPoint>>): ProjectedOutlines {
    val all = outlines.flatten()
    if (all.isEmpty()) return ProjectedOutlines(emptyList(), 0f, 0f)
    val minLat = all.minOf { it.latitude }
    val maxLat = all.maxOf { it.latitude }
    val minLon = all.minOf { it.longitude }
    val maxLon = all.maxOf { it.longitude }
    val cosLat = cos(Math.toRadians((minLat + maxLat) / 2))
    fun project(point: GeoPoint) = Offset(
        x = ((point.longitude - minLon) * METERS_PER_DEGREE_LONGITUDE_AT_EQUATOR * cosLat).toFloat(),
        y = ((maxLat - point.latitude) * METERS_PER_DEGREE_LATITUDE).toFloat(),
    )
    return ProjectedOutlines(
        shapes = outlines.map { outline -> outline.map(::project) },
        width = ((maxLon - minLon) * METERS_PER_DEGREE_LONGITUDE_AT_EQUATOR * cosLat).toFloat(),
        height = ((maxLat - minLat) * METERS_PER_DEGREE_LATITUDE).toFloat(),
    )
}

/** How a [ProjectedOutlines] drawing is scaled and centred inside a box, leaving a margin. */
private class Fit(private val scale: Float, private val originX: Float, private val originY: Float) {
    fun place(point: Offset) = Offset(originX + point.x * scale, originY + point.y * scale)
}

private fun fitInto(drawing: ProjectedOutlines, widthPx: Float, heightPx: Float, paddingPx: Float): Fit {
    val innerWidth = widthPx - 2 * paddingPx
    val innerHeight = heightPx - 2 * paddingPx
    val scale = minOf(innerWidth / max(drawing.width, 1f), innerHeight / max(drawing.height, 1f))
    return Fit(
        scale = scale,
        originX = paddingPx + (innerWidth - drawing.width * scale) / 2,
        originY = paddingPx + (innerHeight - drawing.height * scale) / 2,
    )
}

/**
 * All the producer's plots drawn together on one soft tile, each with its name, and an eye
 * button that opens the real satellite map. It is a drawing, not a live map: it costs nothing
 * to show, works offline, and the real map only loads when the producer asks for it.
 */
@Composable
fun PlotsOverview(plots: List<Plot>, onExpand: () -> Unit, modifier: Modifier = Modifier) {
    val projected = remember(plots) { projectOutlines(plots.map { it.outline }) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Green100),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fit = fitInto(projected, size.width, size.height, PADDING.toPx())
            projected.shapes.forEach { shape ->
                if (shape.isEmpty()) return@forEach
                val path = Path().apply {
                    shape.forEachIndexed { i, p ->
                        val point = fit.place(p)
                        if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                    }
                    close()
                }
                drawPath(path, color = Green200)
                drawPath(path, color = Green800, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))
            }
        }
        if (plots.size <= MAX_LABELS) {
            PlotLabels(names = plots.map { it.name }, projected = projected)
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Neutral0)
                .clickable(role = Role.Button, onClick = onExpand),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_visibility),
                contentDescription = stringResource(R.string.plots_map_open),
                tint = Neutral900,
            )
        }
    }
}

/** Name chips centred over each plot of the drawing. */
@Composable
private fun PlotLabels(names: List<String>, projected: ProjectedOutlines) {
    Layout(
        content = {
            names.forEach { name ->
                Row(
                    modifier = Modifier.clip(CircleShape).background(Neutral0).padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = name, style = MaterialTheme.typography.labelMedium, color = Neutral900, maxLines = 1)
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val fit = fitInto(projected, constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat(), PADDING.toPx())
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val shape = projected.shapes.getOrNull(index).orEmpty()
                if (shape.isEmpty()) return@forEachIndexed
                val center = fit.place(Offset(shape.map { it.x }.average().toFloat(), shape.map { it.y }.average().toFloat()))
                placeable.place((center.x - placeable.width / 2).roundToInt(), (center.y - placeable.height / 2).roundToInt())
            }
        }
    }
}
