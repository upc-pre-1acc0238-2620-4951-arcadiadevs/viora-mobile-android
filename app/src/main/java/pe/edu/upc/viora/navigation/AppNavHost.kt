package pe.edu.upc.viora.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.navigation.HomeGraph
import pe.edu.upc.viora.core.navigation.HomeRoute
import pe.edu.upc.viora.core.navigation.LogbookGraph
import pe.edu.upc.viora.core.navigation.LogbookRoute
import pe.edu.upc.viora.core.navigation.PlaceholderScreen
import pe.edu.upc.viora.core.navigation.PlanGraph
import pe.edu.upc.viora.core.navigation.PlanRoute
import pe.edu.upc.viora.core.navigation.PlotsGraph
import pe.edu.upc.viora.core.navigation.PlotsRoute

/**
 * Composition root of navigation. It lives outside `core/` on purpose: this is the one place
 * allowed to know every feature, so `core/` never depends on `features/`.
 *
 * Each tab currently shows a placeholder; when a feature is ready, replace its
 * `navigation<…Graph>` block with the feature's own graph builder, e.g.
 * `plotsNavGraph(navController)`, declared in `features/<context>/presentation/navigation`.
 */
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeGraph,
        modifier = modifier,
    ) {
        navigation<HomeGraph>(startDestination = HomeRoute) {
            composable<HomeRoute> { PlaceholderScreen(title = stringResource(R.string.nav_home)) }
        }
        navigation<PlotsGraph>(startDestination = PlotsRoute) {
            composable<PlotsRoute> { PlaceholderScreen(title = stringResource(R.string.nav_plots)) }
        }
        navigation<PlanGraph>(startDestination = PlanRoute) {
            composable<PlanRoute> { PlaceholderScreen(title = stringResource(R.string.nav_plan)) }
        }
        navigation<LogbookGraph>(startDestination = LogbookRoute) {
            composable<LogbookRoute> { PlaceholderScreen(title = stringResource(R.string.nav_logbook)) }
        }
    }
}
