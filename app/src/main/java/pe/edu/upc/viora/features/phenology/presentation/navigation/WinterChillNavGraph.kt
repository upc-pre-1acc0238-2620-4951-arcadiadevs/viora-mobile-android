package pe.edu.upc.viora.features.phenology.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.phenology.presentation.ui.WinterChillScreen

@Serializable
data class WinterChillRoute(val plotId: String, val plotName: String = "")

fun NavGraphBuilder.winterChillComposable(navController: NavController) {
    composable<WinterChillRoute> {
        WinterChillScreen(
            onBack = { navController.popBackStack() },
        )
    }
}
