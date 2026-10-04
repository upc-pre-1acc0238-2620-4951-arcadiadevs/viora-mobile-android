package pe.edu.upc.viora.features.telemetry.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.telemetry.presentation.ui.SensorsScreen

/**
 * Route to the sensor nodes screen for a given plot.
 * [plotName] can be passed so the top bar shows it immediately.
 */
@Serializable
data class SensorsRoute(val plotId: String, val plotName: String = "")

fun NavGraphBuilder.sensorsComposable(navController: NavController) {
    composable<SensorsRoute> {
        SensorsScreen(onBack = { navController.popBackStack() })
    }
}
