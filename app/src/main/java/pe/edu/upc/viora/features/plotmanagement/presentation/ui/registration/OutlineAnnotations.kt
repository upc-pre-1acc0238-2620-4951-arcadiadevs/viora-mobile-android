package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.runtime.Composable
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolygonAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

private const val CORNER_RADIUS = 8.0

/**
 * Draws a plot outline on a map: a translucent yellow fill once there are three corners,
 * the edges, and a white dot on every corner. Shared by the tracing map and the read-only
 * maps of the details and review steps, so the plot looks the same in all three. The dots can
 * be hidden ([showCorners] = false) when the caller draws its own, draggable ones on top.
 */
@OptIn(MapboxExperimental::class)
@MapboxMapComposable
@Composable
fun OutlineAnnotations(corners: List<GeoPoint>, showCorners: Boolean = true) {
    val points = corners.map { Point.fromLngLat(it.longitude, it.latitude) }
    if (points.size >= 3) {
        PolygonAnnotation(points = listOf(points + points.first())) {
            fillColor = Harvest300
            fillOpacity = 0.35
            fillOutlineColor = Harvest300
        }
    }
    if (points.size >= 2) {
        PolylineAnnotation(points = if (points.size >= 3) points + points.first() else points) {
            lineColor = Harvest300
            lineWidth = 3.0
        }
    }
    if (showCorners) {
        points.forEach { point ->
            CircleAnnotation(point = point) {
                circleRadius = CORNER_RADIUS
                circleColor = Neutral0
                circleStrokeColor = Neutral900
                circleStrokeWidth = 2.5
            }
        }
    }
}
