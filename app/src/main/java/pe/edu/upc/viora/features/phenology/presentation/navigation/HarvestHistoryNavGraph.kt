package pe.edu.upc.viora.features.phenology.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.climate.presentation.navigation.ClimateRoute
import pe.edu.upc.viora.features.phenology.presentation.ui.HarvestHistoryScreen
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute

/**
 * Route to the alternation screen (harvest history and bearing index) of a plot.
 * [plotName] lets the header show the name immediately.
 */
@Serializable
data class HarvestHistoryRoute(val plotId: String, val plotName: String = "")

fun NavGraphBuilder.harvestHistoryComposable(navController: NavController) {
    composable<HarvestHistoryRoute> { entry ->
        val route = entry.toRoute<HarvestHistoryRoute>()
        HarvestHistoryScreen(
            onBack = { navController.popBackStack() },
            onSensors = { navController.navigate(SensorsRoute(plotId = route.plotId, plotName = route.plotName)) },
            onClimate = { navController.navigate(ClimateRoute(plotId = route.plotId, plotName = route.plotName)) },
        )
    }
}
