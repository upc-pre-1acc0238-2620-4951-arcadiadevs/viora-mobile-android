package pe.edu.upc.viora.features.croploadregulation.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.navigation.LogbookGraph
import pe.edu.upc.viora.core.navigation.PlanGraph
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.RegisterTreeSampleScreen
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.SamplingCompleteScreen
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.SamplingRoundScreen
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.SelectPlotSamplingScreen
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.SamplingSessionViewModel

@Serializable
data object SamplingGraph

@Serializable
data object SelectPlotSamplingRoute

@Serializable
data class SamplingRoundRoute(val plotId: String, val plotName: String)

@Serializable
data class RegisterTreeSampleRoute(
    val plotId: String,
    val plotName: String,
    val treeIndex: Int,
    val defaultIdentifier: String,
)

@Serializable
data class SamplingCompleteRoute(
    val plotId: String,
    val plotName: String,
    val isOffline: Boolean = false,
)

/**
 * Navigation graph for in-field fruit thinning sampling (P51, P52, P53, P54).
 * [plotSilhouetteSlot] allows presentation-only integration with plotmanagement visual silhouette.
 */
fun NavGraphBuilder.samplingNavGraph(
    navController: NavController,
    plotSilhouetteSlot: @Composable (plotId: String, modifier: Modifier) -> Unit,
) {
    navigation<SamplingGraph>(startDestination = SelectPlotSamplingRoute) {
        composable<SelectPlotSamplingRoute> {
            SelectPlotSamplingScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlotSelected = { plotId, plotName ->
                    navController.navigate(SamplingRoundRoute(plotId = plotId, plotName = plotName))
                },
                plotSilhouetteSlot = plotSilhouetteSlot,
            )
        }

        composable<SamplingRoundRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SamplingRoundRoute>()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry<SamplingGraph>()
            }
            val sessionViewModel: SamplingSessionViewModel = hiltViewModel(parentEntry)
            sessionViewModel.initSession(plotId = route.plotId, plotName = route.plotName)
            val sessionState = sessionViewModel.uiState.collectAsStateWithLifecycle().value

            SamplingRoundScreen(
                state = sessionState,
                onNavigateBack = { navController.popBackStack() },
                onAddTree = { nextId, nextIndex ->
                    navController.navigate(
                        RegisterTreeSampleRoute(
                            plotId = route.plotId,
                            plotName = route.plotName,
                            treeIndex = nextIndex,
                            defaultIdentifier = nextId,
                        ),
                    )
                },
                onFinishSampling = {
                    sessionViewModel.submitCurrentBatch(
                        onSuccess = {
                            navController.navigate(
                                SamplingCompleteRoute(
                                    plotId = route.plotId,
                                    plotName = route.plotName,
                                    isOffline = false,
                                ),
                            ) {
                                popUpTo<SamplingRoundRoute> { inclusive = true }
                            }
                        },
                        onOffline = {
                            navController.navigate(
                                SamplingCompleteRoute(
                                    plotId = route.plotId,
                                    plotName = route.plotName,
                                    isOffline = true,
                                ),
                            ) {
                                popUpTo<SamplingRoundRoute> { inclusive = true }
                            }
                        },
                    )
                },
                onContinueLater = {
                    navController.navigate(LogbookGraph) {
                        popUpTo<SamplingGraph> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<RegisterTreeSampleRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<RegisterTreeSampleRoute>()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry<SamplingGraph>()
            }
            val sessionViewModel: SamplingSessionViewModel = hiltViewModel(parentEntry)

            RegisterTreeSampleScreen(
                plotName = route.plotName,
                treeIndex = route.treeIndex,
                defaultIdentifier = route.defaultIdentifier,
                onClose = { navController.popBackStack() },
                onSaveTree = { id, shoots, fruits, circumference ->
                    sessionViewModel.addSample(id, shoots, fruits, circumference)
                    navController.popBackStack()
                },
            )
        }

        composable<SamplingCompleteRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SamplingCompleteRoute>()
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry<SamplingGraph>()
            }
            val sessionViewModel: SamplingSessionViewModel = hiltViewModel(parentEntry)
            val sessionState = sessionViewModel.uiState.collectAsStateWithLifecycle().value

            SamplingCompleteScreen(
                plotName = route.plotName,
                summary = sessionState.submissionSummary,
                evaluatedTreesCount = sessionState.evaluatedTreesCount,
                totalShootsCount = sessionState.totalShootsCount,
                totalFruitsCount = sessionState.totalFruitsCount,
                meanFruitsPerShoot = sessionState.meanFruitsPerShoot,
                isOffline = route.isOffline || sessionState.isOffline,
                onClose = {
                    navController.navigate(LogbookGraph) {
                        popUpTo<SamplingGraph> { inclusive = true }
                    }
                },
                onViewPlotPlan = {
                    navController.navigate(PlanGraph) {
                        popUpTo<SamplingGraph> { inclusive = true }
                    }
                },
                onBackToLogbook = {
                    navController.navigate(LogbookGraph) {
                        popUpTo<SamplingGraph> { inclusive = true }
                    }
                },
            )
        }
    }
}
