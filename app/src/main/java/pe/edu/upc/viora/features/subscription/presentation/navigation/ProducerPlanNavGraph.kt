package pe.edu.upc.viora.features.subscription.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.subscription.presentation.ui.ProducerPlanScreen

/** Host and path of the checkout return link; must match the manifest intent-filter and the backend's VIORA_DEEP_LINK_BASE_URL. */
const val PAYMENT_RETURN_LINK_BASE = "https://viora-platform.onrender.com/payments/return"
const val SUBSCRIPTION_RETURN_LINK_BASE = "https://viora-platform.onrender.com/subscriptions/return"

/** Independent full-screen commercial offer; the agronomic Plan tab remains unchanged. */
@Serializable
data object ProducerPlanRoute

fun NavGraphBuilder.producerPlanComposable(navController: NavController, onLeave: () -> Unit) {
    composable<ProducerPlanRoute>(
        deepLinks = listOf(
            navDeepLink<ProducerPlanRoute>(basePath = PAYMENT_RETURN_LINK_BASE),
            navDeepLink<ProducerPlanRoute>(basePath = SUBSCRIPTION_RETURN_LINK_BASE),
        ),
    ) {
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
