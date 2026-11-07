package pe.edu.upc.viora.features.subscription.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import java.time.Instant
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.subscription.presentation.ui.CooperativeMembershipScreen
import pe.edu.upc.viora.features.subscription.presentation.ui.RedeemCodeScreen

/**
 * P05 → P05b → P06. The sign-up opens it at [RedeemCodeRoute] with the code saved in T02c, once the email is
 * verified; P99 and the Home without access open it empty.
 */
@Serializable
data object CooperativeCodeGraph

/** [code] is the one saved in the onboarding: P05 checks it as soon as it opens and shows P05b. */
@Serializable
data class RedeemCodeRoute(val code: String? = null)

/** P06 gets what it shows from the redemption itself; nothing is cached. */
@Serializable
data class CooperativeMembershipRoute(
    val cooperativeName: String,
    val quotaHectares: Double,
    val endsAtEpochMillis: Long? = null,
    val managerName: String? = null,
)

/**
 * [onLeave] goes on to the rest of the app once the code is redeemed (or when back has nowhere to go),
 * [onRegisterPlot] opens the plot registration from P06, [onOpenMyPlan] is P05 · Ya tienes un plan "Volver a Mi plan".
 */
fun NavGraphBuilder.cooperativeCodeNavGraph(
    navController: NavController,
    onLeave: () -> Unit,
    onRegisterPlot: () -> Unit,
    onOpenProducerPlan: () -> Unit,
    onOpenMyPlan: () -> Unit,
) {
    navigation<CooperativeCodeGraph>(startDestination = RedeemCodeRoute()) {
        composable<RedeemCodeRoute> {
            RedeemCodeScreen(
                onBack = { if (navController.previousBackStackEntry != null) navController.popBackStack() else onLeave() },
                onRedeemed = { membership ->
                    val route = CooperativeMembershipRoute(
                        cooperativeName = membership.cooperativeName,
                        quotaHectares = membership.quotaHectares,
                        endsAtEpochMillis = membership.endsAt?.toEpochMilli(),
                        managerName = membership.managerName,
                    )
                    navController.navigate(route) {
                        // The code is spent: back from P06 must not reopen P05.
                        popUpTo<RedeemCodeRoute> { inclusive = true }
                    }
                },
                onPayPlan = onOpenProducerPlan,
                onOpenMyPlan = onOpenMyPlan,
            )
        }
        composable<CooperativeMembershipRoute> { entry ->
            val route = entry.toRoute<CooperativeMembershipRoute>()
            CooperativeMembershipScreen(
                cooperativeName = route.cooperativeName,
                quotaHectares = route.quotaHectares,
                endsAt = route.endsAtEpochMillis?.let(Instant::ofEpochMilli),
                managerName = route.managerName,
                onRegisterPlot = onRegisterPlot,
                onGoHome = onLeave,
            )
        }
    }
}
