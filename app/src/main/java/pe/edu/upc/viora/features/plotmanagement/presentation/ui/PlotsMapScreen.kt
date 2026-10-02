package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotsUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.OutlineAnnotations
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.frameOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotsViewModel

private const val FIT_PADDING_DP = 64f

/**
 * Every plot of the producer on the real satellite map, framed to fit them all, with the plot
 * names on top. Opened from the eye button of the plots overview; the live map only exists
 * while this screen is open.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun PlotsMapScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: PlotsViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val plots = (state as? PlotsUiState.Content)?.plots.orEmpty()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportState = rememberMapViewportState()
        LaunchedEffect(plots, maxWidth, maxHeight) {
            frameOutline(plots.flatMap { it.outline }, maxWidth.value, maxHeight.value, FIT_PADDING_DP)?.let { framing ->
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
        ) {
            plots.forEach { plot ->
                OutlineAnnotations(plot.outline, showCorners = false)
                val center = plot.outline
                if (center.isNotEmpty()) {
                    PointAnnotation(
                        point = Point.fromLngLat(center.map { it.longitude }.average(), center.map { it.latitude }.average()),
                    ) {
                        textField = plot.name
                        textSize = 14.0
                        textColor = Neutral0
                        textHaloColor = Neutral900
                        textHaloWidth = 1.5
                    }
                }
            }
        }
        CircleIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
            modifier = Modifier.statusBarsPadding().padding(start = 24.dp, top = 12.dp),
        )
    }
}
