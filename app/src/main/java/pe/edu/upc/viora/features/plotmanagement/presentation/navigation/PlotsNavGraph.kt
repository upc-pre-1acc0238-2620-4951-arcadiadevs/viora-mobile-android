package pe.edu.upc.viora.features.plotmanagement.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.navigation.PlotsGraph
import pe.edu.upc.viora.core.navigation.PlotsRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.PlotDetailScreen
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.PlotsMapScreen
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.PlotsScreen
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.AdjustOutlineScreen
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.EditPlotScreen
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.RegisterPlotScreen
import pe.edu.upc.viora.features.phenology.presentation.navigation.HarvestHistoryRoute
import pe.edu.upc.viora.features.phenology.presentation.navigation.harvestHistoryComposable
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.configureNodeComposable
import pe.edu.upc.viora.features.telemetry.presentation.navigation.sensorsComposable
import pe.edu.upc.viora.features.climate.presentation.navigation.ClimateRoute
import pe.edu.upc.viora.features.climate.presentation.navigation.climateComposable

/** The plot registration wizard. It hides the tab bar because it is not a tab root. */
@Serializable
data object RegisterPlotRoute

/** All plots on the real map, opened from the overview of the list. It hides the tab bar too. */
@Serializable
data object PlotsMapRoute

/**
 * One plot's detail. [justSaved] is true when it is reached right after registering the plot,
 * which shows the "plot saved" notice. [plotId] is the plot's UUID.
 */
@Serializable
data class PlotDetailRoute(val plotId: String, val justSaved: Boolean = false)

/** The form that edits a plot's name, variety and planting frame. It hides the tab bar. */
@Serializable
data class EditPlotRoute(val plotId: String)

/** Moves the corners of a registered plot's outline on the map. It hides the tab bar. */
@Serializable
data class AdjustOutlineRoute(val plotId: String)

/** The "Lotes" tab: just the list. The screens it opens are [plotScreens], outside any tab. */
fun NavGraphBuilder.plotsNavGraph(navController: NavController) {
    navigation<PlotsGraph>(startDestination = PlotsRoute) {
        composable<PlotsRoute> {
            PlotsScreen(
                onRegisterPlot = { navController.navigate(RegisterPlotRoute) },
                onOpenMap = { navController.navigate(PlotsMapRoute) },
                onOpenPlot = { id -> navController.navigate(PlotDetailRoute(plotId = id.value)) },
            )
        }
    }
}

/**
 * The screens about a plot (map, registration wizard, detail, edit, outline, sensors and
 * alternation). They sit at the root of the navigation, outside every tab graph, because the
 * producer reaches them from several tabs (Home and Lotes). If they belonged to the Lotes graph,
 * opening one from the Home would push it into the Home's back stack while the tab bar showed
 * Lotes, and the Home tab would then bring that screen back instead of the Home.
 */
fun NavGraphBuilder.plotScreens(navController: NavController) {
    composable<PlotsMapRoute> {
        PlotsMapScreen(onBack = { navController.popBackStack() })
    }
    composable<RegisterPlotRoute> {
        RegisterPlotScreen(
            onLeave = { navController.popBackStack() },
            // The wizard is replaced by the detail, so "back" from there returns to the list.
            onSaved = { id ->
                navController.navigate(PlotDetailRoute(plotId = id.value, justSaved = true)) {
                    popUpTo<RegisterPlotRoute> { inclusive = true }
                }
            },
        )
    }
    composable<PlotDetailRoute> { entry ->
        PlotDetailScreen(
            onBack = { navController.popBackStack() },
            onEdit = { navController.navigate(EditPlotRoute(plotId = entry.toRoute<PlotDetailRoute>().plotId)) },
            onAdjustOutline = { navController.navigate(AdjustOutlineRoute(plotId = entry.toRoute<PlotDetailRoute>().plotId)) },
            onSensors = { navController.navigate(SensorsRoute(plotId = entry.toRoute<PlotDetailRoute>().plotId)) },
            onHarvestHistory = { plotName ->
                navController.navigate(HarvestHistoryRoute(plotId = entry.toRoute<PlotDetailRoute>().plotId, plotName = plotName))
            },
            onClimate = { plotName ->
                navController.navigate(ClimateRoute(plotId = entry.toRoute<PlotDetailRoute>().plotId, plotName = plotName))
            },
        )
    }
    composable<EditPlotRoute> {
        EditPlotScreen(onBack = { navController.popBackStack() })
    }
    composable<AdjustOutlineRoute> {
        AdjustOutlineScreen(onBack = { navController.popBackStack() })
    }
    sensorsComposable(navController)
    configureNodeComposable(navController)
    harvestHistoryComposable(navController)
    climateComposable(navController)
}
