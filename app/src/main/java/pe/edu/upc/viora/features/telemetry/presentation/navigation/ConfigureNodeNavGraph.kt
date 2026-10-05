package pe.edu.upc.viora.features.telemetry.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.telemetry.presentation.ui.ConfigureNodeScreen

@Serializable
data class ConfigureNodeRoute(
    val plotId: String,
    val nodeId: String,
    val plotName: String = "",
)

fun NavGraphBuilder.configureNodeComposable(navController: NavController) {
    composable<ConfigureNodeRoute> { entry ->
        val route = entry.toRoute<ConfigureNodeRoute>()
        ConfigureNodeScreen(
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
            onUnlinked = { navController.popBackStack() },
            plotName = route.plotName,
        )
    }
}
