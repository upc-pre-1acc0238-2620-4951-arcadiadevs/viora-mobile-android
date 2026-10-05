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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

/** The plot's real outline drawn inside a soft green tile; blank when the outline is unknown. */
@Composable
fun PlotSilhouette(
    outline: List<GeoPoint>,
    modifier: Modifier = Modifier,
    showTreeGrid: Boolean = false,
) {
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
                if (showTreeGrid) {
                    clipPath(path) {
                        val step = 7.dp.toPx()
                        val dotRadius = 1.3.dp.toPx()
                        val strokeWidth = 0.9.dp.toPx()
                        var y = step / 2
                        while (y < size.height) {
                            var x = step / 2
                            while (x < size.width) {
                                drawCircle(
                                    color = Green800.copy(alpha = 0.65f),
                                    radius = dotRadius,
                                    center = Offset(x, y),
                                    style = Stroke(width = strokeWidth),
                                )
                                x += step
                            }
                            y += step
                        }
                    }
                }
                drawPath(path, color = Green800, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))
            }
        }
    }
}

/**
 * ViewModel that provides active plot outlines mapped by their UUID string.
 */
@HiltViewModel
class PlotSilhouettesViewModel @Inject constructor(
    observePlots: ObservePlotsUseCase,
) : ViewModel() {
    val outlines: StateFlow<Map<String, List<GeoPoint>>> = observePlots()
        .map { plots -> plots.associate { it.id.value to it.outline } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}

/**
 * Presentation slot component: displays the plot silhouette resolved by [plotId].
 * This allows other features to render a plot thumbnail without coupling to plotmanagement domain models.
 */
@Composable
fun PlotSilhouetteById(
    plotId: String,
    modifier: Modifier = Modifier,
    showTreeGrid: Boolean = false,
    viewModel: PlotSilhouettesViewModel = hiltViewModel(),
) {
    val map = viewModel.outlines.collectAsStateWithLifecycle().value
    PlotSilhouette(outline = map[plotId].orEmpty(), modifier = modifier, showTreeGrid = showTreeGrid)
}


