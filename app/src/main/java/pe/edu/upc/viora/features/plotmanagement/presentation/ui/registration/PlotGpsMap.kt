package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.gestures.gestures
import kotlin.math.roundToInt
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix
import pe.edu.upc.viora.features.plotmanagement.presentation.state.GpsReading
import pe.edu.upc.viora.features.plotmanagement.presentation.state.TracedCorner

/** Zoom with nothing but the producer to show: close enough to see a few rows of trees. */
private const val WALKING_ZOOM = 18.5

/** How far out the camera goes to keep the marked corners in view; beyond it, the producer wins. */
private const val MIN_WALKING_ZOOM = 16.0
private const val FIT_PADDING_DP = 72f
private const val EASE_MILLIS = 800L

/**
 * Where the camera looks while walking: at the producer alone until there are corners, then at
 * the corners and the producer together (so the outline taken so far is always in view), but
 * never so far out that the producer becomes a speck: past [MIN_WALKING_ZOOM] it follows them.
 */
internal fun walkingCamera(
    corners: List<GeoPoint>,
    position: GeoPoint?,
    initialCenter: GeoPoint?,
    widthDp: Float,
    visibleHeightDp: Float,
): MapFraming {
    val everything = if (position == null) corners else corners + position
    val framing = if (everything.size >= 2) frameOutline(everything, widthDp, visibleHeightDp, FIT_PADDING_DP) else null
    return when {
        framing != null && framing.zoom >= MIN_WALKING_ZOOM -> framing
        position != null -> MapFraming(position, if (framing == null) WALKING_ZOOM else MIN_WALKING_ZOOM)
        else -> MapFraming(corners.firstOrNull() ?: initialCenter ?: DefaultMapCenter, WALKING_ZOOM)
    }
}

private val CORNER_BADGE_SIZE = 26.dp
private val POSITION_DOT_SIZE = 22.dp

/**
 * The satellite map of the walking step. The camera follows the producer (the GPS position
 * sits in the middle of the part the sheet leaves visible, thanks to the [bottomInsetPx] padding)
 * and the producer cannot move it: walking is the way to move around. The outline is drawn by
 * Mapbox; the numbered corners and the position of the producer by Compose on top, like the
 * draggable handles of the tracing map.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun PlotGpsMap(
    outline: List<TracedCorner>,
    reading: GpsReading,
    initialCenter: GeoPoint?,
    bottomInsetPx: Int,
    modifier: Modifier = Modifier,
) {
    val viewportState = rememberMapViewportState()
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    var cameraTick by remember { mutableIntStateOf(0) }
    var followingFix by remember { mutableStateOf(false) }
    val fix = when (reading) {
        is GpsReading.Located -> reading.fix
        is GpsReading.Lost -> reading.lastFix
        GpsReading.Searching -> null
    }
    val corners = outline.map { it.point }
    val bottomInset = with(LocalDensity.current) { bottomInsetPx.toDp() }

    BoxWithConstraints(modifier = modifier) {
        val widthDp = maxWidth.value
        val visibleHeightDp = (maxHeight - bottomInset).value

        LaunchedEffect(fix?.point, corners, bottomInsetPx, widthDp, visibleHeightDp) {
            val target = walkingCamera(corners, fix?.point, initialCenter, widthDp, visibleHeightDp)
            val camera = CameraOptions.Builder()
                .center(Point.fromLngLat(target.center.longitude, target.center.latitude))
                .zoom(target.zoom)
                .padding(EdgeInsets(0.0, 0.0, bottomInsetPx.toDouble(), 0.0))
                .build()
            if (fix != null && followingFix) {
                viewportState.easeTo(camera, mapAnimationOptions { duration(EASE_MILLIS) })
            } else {
                // Before the first fix, and when the first one arrives, jump: no flight across the valley.
                viewportState.setCameraOptions(camera)
                if (fix != null) followingFix = true
            }
        }

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState,
            style = { MapStyle(style = Style.SATELLITE_STREETS) },
            compass = {},
            scaleBar = {},
            logo = { Logo(contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = bottomInset + 4.dp)) },
            attribution = {
                Attribution(contentPadding = PaddingValues(start = 92.dp, top = 4.dp, end = 4.dp, bottom = bottomInset + 4.dp))
            },
        ) {
            MapEffect(Unit) { mapView ->
                mapboxMap = mapView.mapboxMap
                mapView.mapboxMap.subscribeCameraChanged { cameraTick++ }
                // The camera follows the GPS: the producer's fingers do not move it.
                mapView.gestures.updateSettings {
                    scrollEnabled = false
                    pinchToZoomEnabled = false
                    rotateEnabled = false
                    pitchEnabled = false
                    doubleTapToZoomInEnabled = false
                    doubleTouchToZoomOutEnabled = false
                    quickZoomEnabled = false
                    pinchScrollEnabled = false
                }
            }
            OutlineAnnotations(corners, showCorners = false)
        }
        mapboxMap?.let { map ->
            GpsOverlay(outline = outline, reading = reading, fix = fix, mapboxMap = map, cameraTick = cameraTick)
        }
    }
}

/** The numbered corners and the producer's position (with the circle of how far off it may be). */
@Composable
private fun GpsOverlay(
    outline: List<TracedCorner>,
    reading: GpsReading,
    fix: GpsFix?,
    mapboxMap: MapboxMap,
    cameraTick: Int,
) {
    val density = LocalDensity.current
    // The number is the order in which the corners were marked, whatever order the outline has.
    outline.sortedBy { it.id }.forEachIndexed { index, corner ->
        val pixel = remember(corner.point, cameraTick) {
            mapboxMap.pixelForCoordinate(Point.fromLngLat(corner.point.longitude, corner.point.latitude))
        }
        AtPixel(x = pixel.x, y = pixel.y) { CornerBadge(number = index + 1) }
    }
    if (fix == null) return

    val lost = reading is GpsReading.Lost
    val color = if (lost) Terracotta500 else Harvest300
    val pixel = remember(fix.point, cameraTick) {
        mapboxMap.pixelForCoordinate(Point.fromLngLat(fix.point.longitude, fix.point.latitude))
    }
    val radiusPx = remember(fix, cameraTick) {
        accuracyRadiusDp(fix.accuracyMeters, fix.point.latitude, mapboxMap.cameraState.zoom).toFloat() * density.density
    }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(pixel.x.toFloat(), pixel.y.toFloat())
        drawCircle(color = color.copy(alpha = 0.22f), radius = radiusPx, center = center)
        drawCircle(
            color = color.copy(alpha = 0.7f),
            radius = radiusPx,
            center = center,
            style = Stroke(
                width = 1.5.dp.toPx(),
                // A solid ring while the position is trusted, dashed once the GPS is lost.
                pathEffect = if (lost) PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())) else null,
            ),
        )
    }
    AtPixel(x = pixel.x, y = pixel.y) {
        Box(modifier = Modifier.size(POSITION_DOT_SIZE).background(color, CircleShape).border(3.dp, Neutral0, CircleShape))
    }
    AtPixel(x = pixel.x, y = pixel.y, extraDy = POSITION_DOT_SIZE + 14.dp) {
        Text(
            text = stringResource(R.string.gps_you),
            style = MaterialTheme.typography.labelMedium,
            color = Neutral50,
            modifier = Modifier.background(Green900, CircleShape).padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

/** Puts [content] centred on a screen pixel (whatever its size), optionally pushed [extraDy] below it. */
@Composable
private fun AtPixel(x: Double, y: Double, extraDy: Dp = 0.dp, content: @Composable () -> Unit) {
    val dy = with(LocalDensity.current) { extraDy.roundToPx() }
    Layout(content = content) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
        layout(placeable.width, placeable.height) {
            placeable.place(x.roundToInt() - placeable.width / 2, y.roundToInt() - placeable.height / 2 + dy)
        }
    }
}

@Composable
private fun CornerBadge(number: Int) {
    Box(
        modifier = Modifier
            .size(CORNER_BADGE_SIZE)
            .background(Neutral0, CircleShape)
            .border(2.5.dp, Neutral900, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = number.toString(), style = MaterialTheme.typography.labelMedium, color = Neutral900)
    }
}
