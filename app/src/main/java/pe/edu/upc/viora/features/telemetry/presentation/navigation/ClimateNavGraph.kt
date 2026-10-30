package pe.edu.upc.viora.features.telemetry.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.navigation.LotSection
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric
import pe.edu.upc.viora.features.telemetry.presentation.ui.PlotClimateScreen
import pe.edu.upc.viora.features.telemetry.presentation.ui.TelemetryDetailScreen

/** Route to "El clima de tu lote" (Figma P90). [plotName] lets the top bar show it at once. */
@Serializable
data class PlotClimateRoute(val plotId: String, val plotName: String = "")

/**
 * Route to the curve of one metric (Figma P91). [metric] is the name of a [TelemetryMetric];
 * it is a string because the domain enum is not serializable.
 */
@Serializable
data class TelemetryDetailRoute(
    val plotId: String,
    val plotName: String = "",
    val metric: String = TelemetryMetric.SOIL_MOISTURE.name,
)

/**
 * The plot climate screens (US19 forecast, US17 sensor series). Like the other plot screens they
 * sit at the root of the navigation, outside every tab, because they are reached from the Home.
 * [onOpenSection] opens another section of the plot; the app shell knows where each one lives.
 */
fun NavGraphBuilder.climateNavGraph(
    navController: NavController,
    onOpenSection: (section: LotSection, plotId: String, plotName: String) -> Unit,
) {
    composable<PlotClimateRoute> {
        PlotClimateScreen(
            onBack = { navController.popBackStack() },
            onOpenMetric = { plotId, plotName, metric ->
                navController.navigate(TelemetryDetailRoute(plotId = plotId, plotName = plotName, metric = metric.name))
            },
            onOpenSensors = { plotId, plotName ->
                onOpenSection(LotSection.SENSORS, plotId, plotName)
            },
            onOpenHarvestHistory = { plotId, plotName ->
                onOpenSection(LotSection.HARVEST, plotId, plotName)
            },
            onOpenWinterChill = { plotId, plotName ->
                onOpenSection(LotSection.WINTER_CHILL, plotId, plotName)
            },
            onOpenThinning = { plotId, plotName ->
                onOpenSection(LotSection.THINNING, plotId, plotName)
            },
        )
    }
    composable<TelemetryDetailRoute> {
        TelemetryDetailScreen(onBack = { navController.popBackStack() })
    }
}
