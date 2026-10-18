package pe.edu.upc.viora.features.telemetry.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.telemetry.presentation.ui.AlertDetailScreen
import pe.edu.upc.viora.features.telemetry.presentation.ui.AlertsCenterScreen

/** Route to the central alerts inbox screen. */
@Serializable
data class AlertsCenterRoute(val plotId: String? = null)

/**
 * Route to the deep detail of an alert.
 * [incidentId] identifies the target agroclimatic incident.
 */
@Serializable
data class AlertDetailRoute(val incidentId: String)

/**
 * Alerts navigation subgraph: Alerts Center and Alert Detail. [onOpenWinterChill] is "Ver mi frío" of a warm winter
 * alert (P80 of its plot).
 */
fun NavGraphBuilder.alertsNavGraph(
    navController: NavController,
    onOpenPlot: (plotId: String) -> Unit = {},
    onOpenWinterChill: (plotId: String, plotName: String) -> Unit = { _, _ -> },
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
            onOpenWinterChill = onOpenWinterChill,
        )
    }
}
