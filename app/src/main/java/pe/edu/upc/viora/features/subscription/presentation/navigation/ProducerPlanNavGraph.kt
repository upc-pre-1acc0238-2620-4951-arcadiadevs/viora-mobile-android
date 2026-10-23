package pe.edu.upc.viora.features.subscription.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.subscription.presentation.ui.ProducerPlanScreen

/** Independent full-screen commercial offer; the agronomic Plan tab remains unchanged. */
@Serializable
data object ProducerPlanRoute

fun NavGraphBuilder.producerPlanComposable(navController: NavController, onLeave: () -> Unit) {
    composable<ProducerPlanRoute> {
        ProducerPlanScreen(
            onBack = { if (!navController.popBackStack()) onLeave() },
            onRedeemCode = {
                if (!navController.popBackStack<RedeemCodeRoute>(inclusive = false)) {
                    navController.navigate(RedeemCodeRoute()) { launchSingleTop = true }
                }
            },
        )
    }
}
