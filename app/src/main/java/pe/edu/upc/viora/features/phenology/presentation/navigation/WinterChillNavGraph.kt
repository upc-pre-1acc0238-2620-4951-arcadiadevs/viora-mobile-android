package pe.edu.upc.viora.features.phenology.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.navigation.LotSection
import pe.edu.upc.viora.features.phenology.presentation.ui.WinterChillScreen

@Serializable
data class WinterChillRoute(val plotId: String, val plotName: String = "")

/** [onOpenSection] opens another section of the plot; the app shell knows where each one lives. [onOpenAlert] the warm-winter alert. */
fun NavGraphBuilder.winterChillComposable(
    navController: NavController,
    onOpenSection: (section: LotSection, plotId: String, plotName: String) -> Unit,
    onOpenAlert: (incidentId: String) -> Unit,
) {
    composable<WinterChillRoute> { entry ->
        val route = entry.toRoute<WinterChillRoute>()
        WinterChillScreen(
            onBack = { navController.popBackStack() },
            onHarvestHistory = {
                onOpenSection(LotSection.HARVEST, route.plotId, route.plotName)
            },
            onSensors = {
                onOpenSection(LotSection.SENSORS, route.plotId, route.plotName)
            },
            onClimate = {
                onOpenSection(LotSection.CLIMATE, route.plotId, route.plotName)
            },
            onThinning = {
                onOpenSection(LotSection.THINNING, route.plotId, route.plotName)
            },
            onWarmWinterAlert = onOpenAlert,
        )
    }
}
