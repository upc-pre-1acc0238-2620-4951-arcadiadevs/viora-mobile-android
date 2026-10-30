package pe.edu.upc.viora.features.telemetry.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.navigation.LotSection
import pe.edu.upc.viora.features.telemetry.presentation.ui.SensorsScreen

/**
 * Route to the sensor nodes screen for a given plot.
 * [plotName] can be passed so the top bar shows it immediately.
 */
@Serializable
data class SensorsRoute(val plotId: String, val plotName: String = "")

/** [onOpenSection] opens another section of the plot; the app shell knows where each one lives. */
fun NavGraphBuilder.sensorsComposable(
    navController: NavController,
    onOpenSection: (section: LotSection, plotId: String, plotName: String) -> Unit,
) {
    composable<SensorsRoute> { entry ->
        val route = entry.toRoute<SensorsRoute>()
        SensorsScreen(
            onBack = { navController.popBackStack() },
            onHarvestHistory = { onOpenSection(LotSection.HARVEST, route.plotId, route.plotName) },
            onClimate = { onOpenSection(LotSection.CLIMATE, route.plotId, route.plotName) },
            onWinterChill = { onOpenSection(LotSection.WINTER_CHILL, route.plotId, route.plotName) },
            onThinning = { onOpenSection(LotSection.THINNING, route.plotId, route.plotName) },
            onNodeClick = { node ->
                navController.navigate(ConfigureNodeRoute(plotId = route.plotId, nodeId = node.id, plotName = route.plotName))
            },
        )
    }
}
