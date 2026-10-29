package pe.edu.upc.viora.features.phenology.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.navigation.LotSection
import pe.edu.upc.viora.features.phenology.presentation.ui.HarvestHistoryScreen

/**
 * Route to the alternation screen (harvest history and bearing index) of a plot.
 * [plotName] lets the header show the name immediately.
 */
@Serializable
data class HarvestHistoryRoute(val plotId: String, val plotName: String = "")

/** [onOpenSection] opens another section of the plot; the app shell knows where each one lives. */
fun NavGraphBuilder.harvestHistoryComposable(
    navController: NavController,
    onOpenSection: (section: LotSection, plotId: String, plotName: String) -> Unit,
) {
    composable<HarvestHistoryRoute> { entry ->
        val route = entry.toRoute<HarvestHistoryRoute>()
        HarvestHistoryScreen(
            onBack = { navController.popBackStack() },
            onSensors = { onOpenSection(LotSection.SENSORS, route.plotId, route.plotName) },
            onClimate = { onOpenSection(LotSection.CLIMATE, route.plotId, route.plotName) },
            onWinterChill = { onOpenSection(LotSection.WINTER_CHILL, route.plotId, route.plotName) },
            onThinning = { onOpenSection(LotSection.THINNING, route.plotId, route.plotName) },
        )
    }
}
