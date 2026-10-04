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
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.sensorsComposable

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

/** The "Lotes" tab: the list, the registration wizard reached from it and each plot's detail. */
fun NavGraphBuilder.plotsNavGraph(navController: NavController) {
    navigation<PlotsGraph>(startDestination = PlotsRoute) {
        composable<PlotsRoute> {
            PlotsScreen(
                onRegisterPlot = { navController.navigate(RegisterPlotRoute) },
                onOpenMap = { navController.navigate(PlotsMapRoute) },
                onOpenPlot = { id -> navController.navigate(PlotDetailRoute(plotId = id.value)) },
            )
        }
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
            )
        }
        composable<EditPlotRoute> {
            EditPlotScreen(onBack = { navController.popBackStack() })
        }
        composable<AdjustOutlineRoute> {
            AdjustOutlineScreen(onBack = { navController.popBackStack() })
        }
        sensorsComposable(navController)
    }
}
