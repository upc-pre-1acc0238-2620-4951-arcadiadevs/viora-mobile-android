package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import pe.edu.upc.viora.core.designsystem.component.PlotSilhouette
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.core.domain.GeoPoint

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

