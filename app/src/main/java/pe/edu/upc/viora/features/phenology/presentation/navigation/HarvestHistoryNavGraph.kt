package pe.edu.upc.viora.features.phenology.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.phenology.presentation.ui.HarvestHistoryScreen

/**
 * Route to the alternation screen (harvest history and bearing index) of a plot.
 * [plotName] lets the header show the name immediately.
 */
@Serializable
data class HarvestHistoryRoute(val plotId: String, val plotName: String = "")

fun NavGraphBuilder.harvestHistoryComposable(navController: NavController) {
    composable<HarvestHistoryRoute> {
        HarvestHistoryScreen(onBack = { navController.popBackStack() })
    }
}
