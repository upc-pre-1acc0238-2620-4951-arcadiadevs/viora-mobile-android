package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.gestures.gestures
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

private const val FIT_PADDING_DP = 40f

/**
 * The traced plot on a read-only satellite map, framed so the whole outline fits, with a caption
 * chip ([caption]) in a corner. Used by the details and review steps so the producer can check
 * that the outline really covers their hectares.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun OutlinePreview(
    corners: List<GeoPoint>,
    caption: String,
    captionAlignment: Alignment,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(28.dp))
            .background(Green100),
    ) {
        val viewportState = rememberMapViewportState()
        // Framing is computed from the size of this box, so the camera is right from the first frame.
        LaunchedEffect(corners, maxWidth, maxHeight) {
            frameOutline(corners, maxWidth.value, maxHeight.value, FIT_PADDING_DP)?.let { framing ->
                viewportState.setCameraOptions(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(framing.center.longitude, framing.center.latitude))
                        .zoom(framing.zoom)
                        .build(),
                )
            }
        }
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = viewportState,
            style = { MapStyle(style = Style.SATELLITE_STREETS) },
            compass = {},
            scaleBar = {},
        ) {
            // It is a picture: no panning, zooming or rotating.
            MapEffect(Unit) { mapView ->
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
            OutlineAnnotations(corners)
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.labelMedium,
            color = Neutral900,
            modifier = Modifier
                .align(captionAlignment)
                .padding(12.dp)
                .clip(CircleShape)
                .background(Neutral0)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
