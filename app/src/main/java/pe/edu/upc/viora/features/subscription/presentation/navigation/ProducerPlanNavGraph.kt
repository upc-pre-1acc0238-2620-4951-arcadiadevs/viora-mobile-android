package pe.edu.upc.viora.features.subscription.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.BuildConfig
import pe.edu.upc.viora.features.subscription.presentation.ui.ProducerPlanScreen

/** Host and path of the checkout return link; must match the manifest intent-filter and the backend's VIORA_DEEP_LINK_BASE_URL. */
val PAYMENT_RETURN_LINK_BASE = "https://${BuildConfig.CHECKOUT_RETURN_HOST}/payments/return"
val SUBSCRIPTION_RETURN_LINK_BASE = "https://${BuildConfig.CHECKOUT_RETURN_HOST}/subscriptions/return"

/**
 * Independent full-screen commercial offer; the agronomic Plan tab remains unchanged. With [renewal]
 * it is "Renueva y amplía" (Figma P102 · Por vencer): the next year of a plan in its last 30 days.
 * [hectares] are the ones chosen in the onboarding (T02d), the starting selection right after the sign-up.
 */
@Serializable
data class ProducerPlanRoute(val renewal: Boolean = false, val hectares: Int? = null)

fun NavGraphBuilder.producerPlanComposable(navController: NavController, onLeave: () -> Unit, onRegisterPlot: () -> Unit) {
    composable<ProducerPlanRoute>(
        deepLinks = listOf(
            navDeepLink<ProducerPlanRoute>(basePath = PAYMENT_RETURN_LINK_BASE),
            navDeepLink<ProducerPlanRoute>(basePath = SUBSCRIPTION_RETURN_LINK_BASE),
        ),
    ) { entry ->
        ProducerPlanScreen(
            renewal = entry.toRoute<ProducerPlanRoute>().renewal,
            onBack = { if (!navController.popBackStack()) onLeave() },
            onGoHome = onLeave,
            onRegisterPlot = onRegisterPlot,
            onRedeemCode = {
                if (!navController.popBackStack<RedeemCodeRoute>(inclusive = false)) {
                    navController.navigate(RedeemCodeRoute()) { launchSingleTop = true }
                }
            },
        )
    }
}
