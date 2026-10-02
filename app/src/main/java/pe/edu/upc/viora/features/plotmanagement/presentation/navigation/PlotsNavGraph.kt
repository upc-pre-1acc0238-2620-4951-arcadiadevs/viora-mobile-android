package pe.edu.upc.viora.features.plotmanagement.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.navigation.PlotsGraph
import pe.edu.upc.viora.core.navigation.PlotsRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.PlotsMapScreen
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.PlotsScreen
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.RegisterPlotScreen

/** The plot registration wizard. It hides the tab bar because it is not a tab root. */
@Serializable
data object RegisterPlotRoute

/** All plots on the real map, opened from the overview of the list. It hides the tab bar too. */
@Serializable
data object PlotsMapRoute

/** The "Lotes" tab: the list, and the registration wizard reached from it. */
fun NavGraphBuilder.plotsNavGraph(navController: NavController) {
    navigation<PlotsGraph>(startDestination = PlotsRoute) {
        composable<PlotsRoute> {
            PlotsScreen(
                onRegisterPlot = { navController.navigate(RegisterPlotRoute) },
                onOpenMap = { navController.navigate(PlotsMapRoute) },
            )
        }
        composable<PlotsMapRoute> {
            PlotsMapScreen(onBack = { navController.popBackStack() })
        }
        composable<RegisterPlotRoute> {
            RegisterPlotScreen(onFinished = { navController.popBackStack() })
        }
    }
}
