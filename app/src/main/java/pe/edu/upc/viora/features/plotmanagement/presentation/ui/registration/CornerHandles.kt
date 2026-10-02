package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ScreenCoordinate
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.presentation.state.TracedCorner
import kotlin.math.roundToInt

private val HANDLE_TOUCH_SIZE = 48.dp
private val HANDLE_DOT_SIZE = 26.dp

/**
 * One draggable dot per corner, drawn by Compose on top of a map that is locked in place.
 * Each handle sits at the screen position of its corner ([MapboxMap.pixelForCoordinate]); while
 * the finger drags it, the screen position is turned back into a coordinate
 * ([MapboxMap.coordinateForPixel]) and reported through [onMove]. The caller decides if that
 * place is valid: when it is refused the corner does not move, so the handle stays put.
 *
 * [cameraTick] changes whenever the map camera changes, to recompute the screen positions.
 */
@Composable
fun CornerHandles(
    corners: List<TracedCorner>,
    mapboxMap: MapboxMap,
    cameraTick: Int,
    onMove: (id: Int, point: GeoPoint) -> Unit,
) {
    corners.forEach { corner ->
        // Not remembered on `cameraTick` alone: the corner itself also moves.
        val pixel = remember(corner.point, cameraTick) {
            mapboxMap.pixelForCoordinate(Point.fromLngLat(corner.point.longitude, corner.point.latitude))
        }
        CornerHandle(
            screenPosition = Offset(pixel.x.toFloat(), pixel.y.toFloat()),
            onDragTo = { finger ->
                val point = mapboxMap.coordinateForPixel(ScreenCoordinate(finger.x.toDouble(), finger.y.toDouble()))
                onMove(corner.id, GeoPoint(latitude = point.latitude(), longitude = point.longitude()))
            },
            key = corner.id,
        )
    }
}

@Composable
private fun CornerHandle(screenPosition: Offset, onDragTo: (Offset) -> Unit, key: Int) {
    val position by rememberUpdatedState(screenPosition)
    val dragTo by rememberUpdatedState(onDragTo)
    var finger by remember { mutableStateOf(Offset.Zero) }
    val half = with(androidx.compose.ui.platform.LocalDensity.current) { HANDLE_TOUCH_SIZE.toPx() / 2 }
    Box(
        modifier = Modifier
            .offset { IntOffset((screenPosition.x - half).roundToInt(), (screenPosition.y - half).roundToInt()) }
            .size(HANDLE_TOUCH_SIZE)
            .pointerInput(key) {
                detectDragGestures(
                    onDragStart = { finger = position },
                    onDrag = { change, amount ->
                        change.consume()
                        // The finger moves freely; the handle only follows when the place is valid.
                        finger += amount
                        dragTo(finger)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(HANDLE_DOT_SIZE)
                .background(Neutral0, CircleShape)
                .border(2.5.dp, Neutral900, CircleShape),
        )
    }
}
