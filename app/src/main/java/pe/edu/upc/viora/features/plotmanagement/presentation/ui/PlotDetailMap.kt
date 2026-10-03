package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.gestures.gestures
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.OutlineAnnotations
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.frameOutline

/** Free space kept around the outline when it is framed, so it clears the buttons and the sheet. */
private const val FIT_PADDING_DP = 72f

/** How far out and in the producer can go: from the whole valley to a single tree row. */
private const val MIN_ZOOM = 11.0
private const val MAX_ZOOM = 20.0
private const val EASE_MILLIS = 350L

/**
 * The satellite map behind the plot detail. It fills the screen; the bottom [coveredHeight] is
 * hidden by the sheet, so the camera is padded by it: the outline is centred (and the Mapbox
 * logo placed) in the part that is actually visible.
 *
 * With [interactive] false the map is a picture, framed on the outline. With it true the
 * producer can pan and zoom (never rotate or tilt); the outline only follows the sheet, keeping
 * the zoom, and when [interactive] goes back to false the whole outline is framed again.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun PlotDetailMap(
    corners: List<GeoPoint>,
    coveredHeight: Dp,
    interactive: Boolean,
    modifier: Modifier = Modifier,
) {
    val viewportState = rememberMapViewportState()
    val density = LocalDensity.current
    var placed by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        val widthDp = maxWidth.value
        val visibleHeightDp = (maxHeight - coveredHeight).value
        val padding = EdgeInsets(0.0, 0.0, with(density) { coveredHeight.toPx() }.toDouble(), 0.0)

        LaunchedEffect(corners, widthDp, visibleHeightDp, interactive) {
            if (placed && interactive) {
                // Keep whatever the producer is looking at; only make room for the new sheet position.
                viewportState.easeTo(
                    CameraOptions.Builder().padding(padding).build(),
                    mapAnimationOptions { duration(EASE_MILLIS) },
                )
                return@LaunchedEffect
            }
            val framing = frameOutline(corners, widthDp, visibleHeightDp, FIT_PADDING_DP) ?: return@LaunchedEffect
            val camera = CameraOptions.Builder()
                .center(Point.fromLngLat(framing.center.longitude, framing.center.latitude))
                .zoom(framing.zoom)
                .padding(padding)
                .build()
            if (placed) {
                viewportState.easeTo(camera, mapAnimationOptions { duration(EASE_MILLIS) })
            } else {
                // First frame: no animation, so the map never opens on the wrong place.
                viewportState.setCameraOptions(camera)
                placed = true
            }
        }

        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState,
            style = { MapStyle(style = Style.SATELLITE_STREETS) },
            compass = {},
            scaleBar = {},
            logo = { Logo(contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = coveredHeight + 4.dp)) },
            attribution = {
                Attribution(contentPadding = PaddingValues(start = 92.dp, top = 4.dp, end = 4.dp, bottom = coveredHeight + 4.dp))
            },
        ) {
            MapEffect(Unit) { mapView ->
                mapView.mapboxMap.setBounds(CameraBoundsOptions.Builder().minZoom(MIN_ZOOM).maxZoom(MAX_ZOOM).build())
            }
            MapEffect(interactive) { mapView ->
                mapView.gestures.updateSettings {
                    scrollEnabled = interactive
                    pinchToZoomEnabled = interactive
                    pinchScrollEnabled = interactive
                    doubleTapToZoomInEnabled = interactive
                    doubleTouchToZoomOutEnabled = interactive
                    quickZoomEnabled = interactive
                    rotateEnabled = false
                    pitchEnabled = false
                }
            }
            OutlineAnnotations(corners)
        }
    }
}
