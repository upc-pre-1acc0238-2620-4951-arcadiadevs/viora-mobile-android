package pe.edu.upc.viora.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import pe.edu.upc.viora.features.climate.presentation.navigation.ClimateRoute
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.LogbookScreen
import pe.edu.upc.viora.features.home.presentation.ui.HomeScreen
import pe.edu.upc.viora.features.phenology.presentation.navigation.HarvestHistoryRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.PlotDetailRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.RegisterPlotRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.plotScreens
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.plotsNavGraph
import pe.edu.upc.viora.features.telemetry.presentation.navigation.AlertsCenterRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.alertsNavGraph

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
            composable<HomeRoute> {
                HomeScreen(
                    onRegisterPlot = { navController.navigate(RegisterPlotRoute) },
                    onOpenPlot = { id -> navController.navigate(PlotDetailRoute(plotId = id.value)) },
                    onOpenAlternation = { id, name -> navController.navigate(HarvestHistoryRoute(plotId = id.value, plotName = name)) },
                    onOpenWeatherForecast = { id, name -> navController.navigate(ClimateRoute(plotId = id.value, plotName = name)) },
                    onOpenPlots = {
                        navController.navigate(PlotsGraph) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenAlerts = { navController.navigate(AlertsCenterRoute) },
                )
            }
        }
        plotsNavGraph(navController)
        plotScreens(navController)
        alertsNavGraph(
            navController = navController,
            onOpenPlot = { plotId -> navController.navigate(PlotDetailRoute(plotId = plotId)) },
        )
        navigation<PlanGraph>(startDestination = PlanRoute) {
            composable<PlanRoute> { PlaceholderScreen(title = stringResource(R.string.nav_plan)) }
        }
        navigation<LogbookGraph>(startDestination = LogbookRoute) {
            composable<LogbookRoute> { LogbookScreen() }
        }
    }
}
