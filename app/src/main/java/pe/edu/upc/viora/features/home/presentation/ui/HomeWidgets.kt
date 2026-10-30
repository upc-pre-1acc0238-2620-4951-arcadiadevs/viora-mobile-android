package pe.edu.upc.viora.features.home.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

/**
 * The Home cards that other contexts draw with their own data: weather and soil moisture (telemetry) and
 * "Tu alternancia" (phenology). The shell fills them when it registers the Home, so the Home never imports another
 * context's screens; each card draws nothing while its context has nothing to show.
 */
@Immutable
class HomeWidgets(
    val weather: @Composable (plotId: String?, onClick: () -> Unit, modifier: Modifier) -> Unit = { _, _, _ -> },
    val soilMoisture: @Composable (plotId: String?, onClick: () -> Unit, modifier: Modifier) -> Unit = { _, _, _ -> },
    val alternation: @Composable (
        plotId: String,
        plotName: String,
        areaHectares: Double,
        onOpen: () -> Unit,
        modifier: Modifier,
    ) -> Unit = { _, _, _, _, _ -> },
)
