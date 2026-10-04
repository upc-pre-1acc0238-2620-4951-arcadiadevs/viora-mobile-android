package pe.edu.upc.viora.features.telemetry.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.telemetry.presentation.ui.AlertDetailScreen
import pe.edu.upc.viora.features.telemetry.presentation.ui.AlertsCenterScreen

/** Route to the central alerts inbox screen. */
@Serializable
data object AlertsCenterRoute

/**
 * Route to the deep detail of an alert.
 * [incidentId] identifies the target agroclimatic incident.
 */
@Serializable
data class AlertDetailRoute(val incidentId: String)

/** Alerts navigation subgraph: Alerts Center and Alert Detail. */
fun NavGraphBuilder.alertsNavGraph(
    navController: NavController,
    onOpenPlot: (plotId: String) -> Unit = {},
) {
    composable<AlertsCenterRoute> {
        AlertsCenterScreen(
            onBack = { navController.popBackStack() },
            onOpenDetail = { incidentId ->
                navController.navigate(AlertDetailRoute(incidentId = incidentId))
            },
        )
    }

    composable<AlertDetailRoute> {
        AlertDetailScreen(
            onBack = { navController.popBackStack() },
            onOpenPlot = { plotId -> onOpenPlot(plotId) },
        )
    }
}
