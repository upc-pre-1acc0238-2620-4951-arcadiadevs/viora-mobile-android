package pe.edu.upc.viora.features.subscription.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import java.time.Instant
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.subscription.presentation.ui.ExtensionAppliedScreen
import pe.edu.upc.viora.features.subscription.presentation.ui.ExtensionCodeScreen
import pe.edu.upc.viora.features.subscription.presentation.ui.PlanExtensionScreen

/** Return link of an extension payment; the backend sends Mercado Pago here, under the plan's return path. */
val EXTENSION_RETURN_LINK_BASE = "$PAYMENT_RETURN_LINK_BASE/extension"

/** P102 · Ampliar mi plan, the single way into growing a plan (P99, P20 and P25 open it). */
@Serializable
data object PlanExtensionRoute

/** P102 opened by the Mercado Pago return link: it shows the result of the payment. */
@Serializable
data object PlanExtensionReturnRoute

/** P103 · Ampliar con código. */
@Serializable
data object ExtensionCodeRoute

/** P104 · Cupo ampliado, with what the redeemed code added. */
@Serializable
data class ExtensionAppliedRoute(
    val addedHectares: String,
    val quotaHectares: String,
    val endsAtEpochSecond: Long?,
    val code: String,
)

/**
 * The plan extension screens. [onRegisterPlot] opens the plot registration, [onViewPlan] returns to
 * P99, [onRenew] opens P02 in renewal mode and [onBuyPlan] opens P02 to buy a plan.
 */
fun NavGraphBuilder.planExtensionGraph(
    navController: NavController,
    onRegisterPlot: () -> Unit,
    onViewPlan: () -> Unit,
    onRenew: () -> Unit,
    onBuyPlan: () -> Unit,
) {
    // Leaving a result closes every extension screen first, so Back never returns to a spent payment or code.
    val registerPlot = { navController.popExtensionScreens(); onRegisterPlot() }
    val viewPlan = { navController.popExtensionScreens(); onViewPlan() }
    val handOver = { open: () -> Unit ->
        // P102 only decided where to go: it must not stay under the screen it opened.
        navController.popBackStack<PlanExtensionRoute>(inclusive = true)
        open()
    }
    composable<PlanExtensionRoute> {
        PlanExtensionScreen(
            onBack = { navController.popBackStack() },
            onUseCode = {
                navController.navigate(ExtensionCodeRoute) { popUpTo<PlanExtensionRoute> { inclusive = true } }
            },
            onRenew = { handOver(onRenew) },
            onBuyPlan = { handOver(onBuyPlan) },
            onRegisterPlot = registerPlot,
            onViewPlan = viewPlan,
        )
    }
    composable<PlanExtensionReturnRoute>(deepLinks = listOf(navDeepLink<PlanExtensionReturnRoute>(basePath = EXTENSION_RETURN_LINK_BASE))) {
        PlanExtensionScreen(
            onBack = { if (!navController.popBackStack()) onViewPlan() },
            onUseCode = { navController.navigate(ExtensionCodeRoute) },
            onRenew = onRenew,
            onBuyPlan = onBuyPlan,
            onRegisterPlot = registerPlot,
            onViewPlan = viewPlan,
            returning = true,
        )
    }
    composable<ExtensionCodeRoute> {
        ExtensionCodeScreen(
            onBack = { navController.popBackStack() },
            onApplied = { extension, code ->
                navController.navigate(
                    ExtensionAppliedRoute(
                        addedHectares = extension.addedHectares.toPlainString(),
                        quotaHectares = extension.quotaHectares.toPlainString(),
                        endsAtEpochSecond = extension.endsAt?.epochSecond,
                        code = code,
                    ),
                ) {
                    // The code is spent: back from P104 must not reopen P103.
                    popUpTo<ExtensionCodeRoute> { inclusive = true }
                }
            },
        )
    }
    composable<ExtensionAppliedRoute> { entry ->
        val route = entry.toRoute<ExtensionAppliedRoute>()
        ExtensionAppliedScreen(
            addedHectares = route.addedHectares.toBigDecimal(),
            quotaHectares = route.quotaHectares.toBigDecimal(),
            endsAt = route.endsAtEpochSecond?.let(Instant::ofEpochSecond),
            code = route.code,
            onRegisterPlot = registerPlot,
            onViewPlan = viewPlan,
        )
    }
}

private val EXTENSION_ROUTES = listOf(
    PlanExtensionRoute::class, PlanExtensionReturnRoute::class, ExtensionCodeRoute::class, ExtensionAppliedRoute::class,
)

/** Pops the extension screens on top of the back stack, never the last screen of the app. */
private fun NavController.popExtensionScreens() {
    while (currentBackStackEntry?.destination?.let { destination -> EXTENSION_ROUTES.any { destination.hasRoute(it) } } == true) {
        if (!popBackStack()) break
    }
}
