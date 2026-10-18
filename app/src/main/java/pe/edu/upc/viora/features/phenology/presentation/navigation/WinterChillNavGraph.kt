package pe.edu.upc.viora.features.phenology.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.phenology.presentation.ui.WinterChillScreen
import pe.edu.upc.viora.features.telemetry.presentation.navigation.AlertDetailRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.PlotClimateRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.features.croploadregulation.presentation.navigation.ThinningPlanRoute

@Serializable
data class WinterChillRoute(val plotId: String, val plotName: String = "")

fun NavGraphBuilder.winterChillComposable(navController: NavController) {
    composable<WinterChillRoute> { entry ->
        val route = entry.toRoute<WinterChillRoute>()
        WinterChillScreen(
            onBack = { navController.popBackStack() },
            onHarvestHistory = {
                navController.navigate(HarvestHistoryRoute(plotId = route.plotId, plotName = route.plotName))
            },
            onSensors = {
                navController.navigate(SensorsRoute(plotId = route.plotId, plotName = route.plotName))
            },
            onClimate = {
                navController.navigate(PlotClimateRoute(plotId = route.plotId, plotName = route.plotName))
            },
            onThinning = {
                navController.navigate(ThinningPlanRoute(plotId = route.plotId, plotName = route.plotName))
            },
            onWarmWinterAlert = { incidentId -> navController.navigate(AlertDetailRoute(incidentId = incidentId)) },
        )
    }
}
