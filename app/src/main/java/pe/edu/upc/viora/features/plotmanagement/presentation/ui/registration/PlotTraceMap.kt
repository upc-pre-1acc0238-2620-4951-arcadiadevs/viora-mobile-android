package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.presentation.state.TracedCorner

/** Where the map opens when the producer has nothing to centre on: the Tacna olive valley. */
val DefaultMapCenter = GeoPoint(latitude = -18.10, longitude = -70.40)

private const val DEFAULT_ZOOM = 12.0
private const val CENTERED_ZOOM = 16.0

/**
 * Satellite map where the producer taps to mark the corners of a plot. The corners are owned
 * by the caller ([corners]); this composable only draws them and reports each tap. The camera
 * ([viewportState]) is also owned by the caller so it can read the map centre.
 *
 * [bottomInsetPx] is the height covered by the bottom sheet: the camera is padded by it, so the
 * map centre (and the Mapbox logo) stay in the part of the map that is actually visible.
 * With [moving] the map is locked and the corners ([outline]) become draggable handles (see
 * [CornerHandles]); taps do not add corners then.
 * The compass and scale bar are left out: they sat under the header buttons and the sheet.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun PlotTraceMap(
    corners: List<GeoPoint>,
    initialCenter: GeoPoint?,
    viewportState: MapViewportState,
    onCornerTapped: (GeoPoint) -> Unit,
    bottomInsetPx: Int,
    moving: Boolean,
    outline: List<TracedCorner>,
    onCornerMoved: (id: Int, point: GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    var cameraTick by remember { mutableIntStateOf(0) }
    // Centre once, when the first known centre arrives (existing plots load asynchronously).
    LaunchedEffect(initialCenter) {
        val center = initialCenter ?: DefaultMapCenter
        viewportState.setCameraOptions(
            CameraOptions.Builder()
                .center(Point.fromLngLat(center.longitude, center.latitude))
                .zoom(if (initialCenter != null) CENTERED_ZOOM else DEFAULT_ZOOM)
                .build(),
        )
    }
    LaunchedEffect(bottomInsetPx) {
        viewportState.setCameraOptions(
            CameraOptions.Builder().padding(EdgeInsets(0.0, 0.0, bottomInsetPx.toDouble(), 0.0)).build(),
        )
    }
    val tapListener = remember(onCornerTapped, moving) {
        OnMapClickListener { point ->
            if (!moving) onCornerTapped(GeoPoint(latitude = point.latitude(), longitude = point.longitude()))
            true
        }
    }
    val bottomInset = with(LocalDensity.current) { bottomInsetPx.toDp() }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState,
            onMapClickListener = tapListener,
            style = { MapStyle(style = Style.SATELLITE_STREETS) },
            compass = {},
            scaleBar = {},
            logo = { Logo(contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = bottomInset + 4.dp)) },
            attribution = { Attribution(contentPadding = PaddingValues(start = 92.dp, top = 4.dp, end = 4.dp, bottom = bottomInset + 4.dp)) },
        ) {
            // The map is completely still while corners are moved, so their screen positions stay valid.
            MapEffect(moving) { mapView ->
                mapView.gestures.updateSettings {
                    scrollEnabled = !moving
                    pinchToZoomEnabled = !moving
                    rotateEnabled = !moving
                    pitchEnabled = !moving
                    doubleTapToZoomInEnabled = !moving
                    doubleTouchToZoomOutEnabled = !moving
                    quickZoomEnabled = !moving
                }
            }
            MapEffect(Unit) { mapView ->
                mapboxMap = mapView.mapboxMap
                mapView.mapboxMap.subscribeCameraChanged { cameraTick++ }
            }
            OutlineAnnotations(corners, showCorners = !moving)
        }
        mapboxMap?.takeIf { moving }?.let { map ->
            CornerHandles(corners = outline, mapboxMap = map, cameraTick = cameraTick, onMove = onCornerMoved)
        }
    }
}
