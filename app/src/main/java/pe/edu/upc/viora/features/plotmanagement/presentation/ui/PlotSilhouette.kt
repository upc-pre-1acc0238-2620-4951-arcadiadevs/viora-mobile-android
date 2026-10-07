package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

/**
 * The plot thumbnail of Figma P20 "Silueta": the same drawing as the Home plot card (terrain,
 * outline and trees, see PlotArt) at 62 % inside a 72 dp tile; the tile alone when the outline
 * is unknown. [showTreeGrid] = false leaves the trees out.
 */
@Composable
fun PlotSilhouette(
    outline: List<GeoPoint>,
    modifier: Modifier = Modifier,
    showTreeGrid: Boolean = true,
) {
    val points = remember(outline) { normalizeOutline(outline) }
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Green100),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTerrain()
            val inset = 10.dp.toPx()
            drawPlot(
                points = points,
                area = Rect(inset, inset, size.width - inset, size.height - inset),
                trees = if (showTreeGrid) TreeGrid.Thumbnail else null,
                borderWidth = 1.dp,
            )
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


