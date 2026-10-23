package pe.edu.upc.viora.features.subscription.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.subscription.presentation.ui.CooperativeMembershipScreen
import pe.edu.upc.viora.features.subscription.presentation.ui.RedeemCodeScreen

/**
 * P05 → P06. The sign-up opens it at [RedeemCodeRoute] with the code saved in T02c, once the email is
 * verified; the other ways in (P02 and the Home without access) do not exist in the app yet.
 */
@Serializable
data object CooperativeCodeGraph

/** [code] is the one saved in the onboarding: P05 redeems it as soon as it opens. */
@Serializable
data class RedeemCodeRoute(val code: String? = null)

/** P06 gets what it shows from the redemption itself; nothing is cached. */
@Serializable
data class CooperativeMembershipRoute(val cooperativeName: String, val quotaHectares: Double)

/**
 * [onLeave] goes on to the rest of the app once the code is redeemed (or when back has nowhere to go),
 * [onRegisterPlot] opens the plot registration from P06.
 */
fun NavGraphBuilder.cooperativeCodeNavGraph(
    navController: NavController,
    onLeave: () -> Unit,
    onRegisterPlot: () -> Unit,
    onOpenProducerPlan: () -> Unit,
) {
    navigation<CooperativeCodeGraph>(startDestination = RedeemCodeRoute()) {
        composable<RedeemCodeRoute> {
            RedeemCodeScreen(
                onBack = { if (navController.previousBackStackEntry != null) navController.popBackStack() else onLeave() },
                onRedeemed = { membership ->
                    navController.navigate(CooperativeMembershipRoute(membership.cooperativeName, membership.quotaHectares)) {
                        // The code is spent: back from P06 must not reopen P05.
                        popUpTo<RedeemCodeRoute> { inclusive = true }
                    }
                },
                onPayPlan = onOpenProducerPlan,
            )
        }
        composable<CooperativeMembershipRoute> { entry ->
            val route = entry.toRoute<CooperativeMembershipRoute>()
            CooperativeMembershipScreen(
                cooperativeName = route.cooperativeName,
                quotaHectares = route.quotaHectares,
                onRegisterPlot = onRegisterPlot,
                onGoHome = onLeave,
            )
        }
    }
}
