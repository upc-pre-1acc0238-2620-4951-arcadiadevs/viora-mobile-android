package pe.edu.upc.viora.features.climate.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.climate.presentation.ui.ClimateScreen
import pe.edu.upc.viora.features.phenology.presentation.navigation.HarvestHistoryRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute

/**
 * Route to the 7-day weather forecast screen of an olive plot.
 * [plotName] allows the header to display immediately before network sync.
 */
@Serializable
data class ClimateRoute(val plotId: String, val plotName: String = "")

fun NavGraphBuilder.climateComposable(navController: NavController) {
    composable<ClimateRoute> { entry ->
        val route = entry.toRoute<ClimateRoute>()
        ClimateScreen(
            onBack = { navController.popBackStack() },
            onHarvestHistory = {
                navController.navigate(HarvestHistoryRoute(plotId = route.plotId, plotName = route.plotName))
            },
            onSensors = {
                navController.navigate(SensorsRoute(plotId = route.plotId, plotName = route.plotName))
            },
        )
    }
}
