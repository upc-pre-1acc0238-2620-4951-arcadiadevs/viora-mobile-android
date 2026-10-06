package pe.edu.upc.viora.features.harvestsettlement.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.SettleHarvestScreen

/** Route to the settle harvest form (P71) of a plot for a campaign. */
@Serializable
data class SettleHarvestRoute(val plotId: String, val campaignYear: Int)

/**
 * Registers the settle form. The three callbacks receive the plot id and campaign year: the host
 * decides where the producer goes once the harvest is settled, queued offline, or already closed.
 */
fun NavGraphBuilder.settleHarvestComposable(
    navController: NavController,
    onSettled: (plotId: String, year: Int) -> Unit,
    onQueued: (plotId: String, year: Int) -> Unit,
    onViewReceipt: (plotId: String, year: Int) -> Unit,
) {
    composable<SettleHarvestRoute> {
        SettleHarvestScreen(
            onClose = { navController.popBackStack() },
            onSettled = onSettled,
            onQueued = onQueued,
            onViewReceipt = onViewReceipt,
        )
    }
}
