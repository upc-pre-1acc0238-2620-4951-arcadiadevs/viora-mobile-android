package pe.edu.upc.viora.features.telemetry.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.designsystem.component.AssistantSheetSlot
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

const val ALERT_DETAIL_LINK = "viora://critical-alert"

/**
 * Alerts navigation subgraph: Alerts Center and Alert Detail. [onOpenWinterChill] is "Ver mi frío" of a warm winter
 * alert (P80 of its plot), and [assistantSheet] the sheet of its "Pregúntale a Viora" card (P123).
 */
fun NavGraphBuilder.alertsNavGraph(
    navController: NavController,
    onOpenPreferences: () -> Unit,
    onOpenPlot: (plotId: String) -> Unit = {},
    onOpenWinterChill: (plotId: String, plotName: String) -> Unit = { _, _ -> },
    assistantSheet: AssistantSheetSlot? = null,
) {
    composable<AlertsCenterRoute> {
        AlertsCenterScreen(
            onBack = { navController.popBackStack() },
            onOpenDetail = { incidentId ->
                navController.navigate(AlertDetailRoute(incidentId = incidentId))
            },
            onOpenPreferences = onOpenPreferences,
        )
    }

    composable<AlertDetailRoute>(deepLinks = listOf(navDeepLink<AlertDetailRoute>(basePath = ALERT_DETAIL_LINK))) {
        AlertDetailScreen(
            onBack = { navController.popBackStack() },
            onOpenPlot = { plotId -> onOpenPlot(plotId) },
            onOpenWinterChill = onOpenWinterChill,
            assistantSheet = assistantSheet,
        )
    }
}
