package pe.edu.upc.viora.features.subscription.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.subscription.presentation.ui.CooperativeMembershipScreen
import pe.edu.upc.viora.features.subscription.presentation.ui.RedeemCodeScreen

/** P05 → P06. No screen opens it yet: the sign-up flow that leads here (P02) does not exist in the app. */
@Serializable
data object CooperativeCodeGraph

@Serializable
data object RedeemCodeRoute

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
) {
    navigation<CooperativeCodeGraph>(startDestination = RedeemCodeRoute) {
        composable<RedeemCodeRoute> {
            RedeemCodeScreen(
                onBack = { if (navController.previousBackStackEntry != null) navController.popBackStack() else onLeave() },
                onRedeemed = { membership ->
                    navController.navigate(CooperativeMembershipRoute(membership.cooperativeName, membership.quotaHectares)) {
                        // The code is spent: back from P06 must not reopen P05.
                        popUpTo<RedeemCodeRoute> { inclusive = true }
                    }
                },
                // Paying the individual plan is not built yet (subscription payment, another feature).
                onPayPlan = {},
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
