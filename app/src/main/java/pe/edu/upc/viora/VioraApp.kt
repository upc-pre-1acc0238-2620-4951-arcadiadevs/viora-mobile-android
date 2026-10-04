package pe.edu.upc.viora

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pe.edu.upc.viora.core.designsystem.component.TabBarAction
import pe.edu.upc.viora.core.designsystem.component.TabBarItem
import pe.edu.upc.viora.core.designsystem.component.TabBarMode
import pe.edu.upc.viora.core.designsystem.component.VioraTabBar
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarActionColors
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.navigation.HomeRoute
import pe.edu.upc.viora.core.navigation.TopLevelDestination
import pe.edu.upc.viora.features.home.presentation.tour.HomeTourOverlay
import pe.edu.upc.viora.features.home.presentation.tour.HomeTourTarget
import pe.edu.upc.viora.features.home.presentation.tour.HomeTourTargets
import pe.edu.upc.viora.features.home.presentation.tour.LocalHomeTourTargets
import pe.edu.upc.viora.features.home.presentation.tour.homeTourTarget
import pe.edu.upc.viora.features.home.presentation.viewmodel.HomeTourViewModel
import pe.edu.upc.viora.features.phenology.presentation.navigation.HarvestHistoryRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.PlotDetailRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.navigation.AppNavHost

/** App shell: navigation host plus the producer's floating tab bar on top-level screens. */
@Composable
fun VioraApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    val tabs = TopLevelDestination.entries
    val tourTargets = remember { HomeTourTargets() }
    val tourViewModel: HomeTourViewModel = hiltViewModel()
    val tourPending by tourViewModel.isPending.collectAsStateWithLifecycle()
    var tourRunning by remember { mutableStateOf(false) }
    // -1 on the plot screens (detail, alternation...): they belong to no tab, they are opened on
    // top of the tab the producer was in, which stays highlighted.
    val tabIndex = tabs.indexOfFirst { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.graph::class) } == true
    }
    var originIndex by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(tabIndex) { if (tabIndex >= 0) originIndex = tabIndex }
    val onPlotScreen = tabIndex < 0
    val selectedIndex = if (onPlotScreen) originIndex else tabIndex
    // Tab roots show the bar, and so do the detail screens the design keeps it on (the plot
    // detail, sensors, alternation); the rest (wizards, full-screen maps) take the whole screen.
    val showTabBar = tabs.any { currentDestination?.hasRoute(it.startRoute) == true } ||
        currentDestination?.hasRoute<PlotDetailRoute>() == true ||
        currentDestination?.hasRoute<SensorsRoute>() == true ||
        currentDestination?.hasRoute<HarvestHistoryRoute>() == true

    val items = tabs.map { TabBarItem(icon = it.icon, label = stringResource(it.label)) }
    val actionLabel = stringResource(R.string.nav_action_add)
    val actions = recordActions()

    var barMode by remember { mutableStateOf(TabBarMode.Rest) }
    LaunchedEffect(selectedIndex, showTabBar) { barMode = TabBarMode.Rest }
    BackHandler(enabled = barMode == TabBarMode.Actions) { barMode = TabBarMode.Rest }

    // The tour opens the first time the Home has data on screen (a plots section and the tab bar).
    val onHome = currentDestination?.hasRoute(HomeRoute::class) == true
    val tourReady = HomeTourTarget.Today in tourTargets.rects && HomeTourTarget.Plots in tourTargets.rects &&
        HomeTourTarget.Register in tourTargets.rects
    LaunchedEffect(tourPending, onHome, tourReady, barMode) {
        if (tourPending == true && onHome && tourReady && barMode == TabBarMode.Rest) tourRunning = true
    }

    // No insets here: each screen decides how to use the area under the system bars (maps go
    // beneath them, lists pad themselves), so the status bar can stay transparent.
    CompositionLocalProvider(LocalHomeTourTargets provides tourTargets) {
    Scaffold(modifier = modifier.fillMaxSize(), contentWindowInsets = WindowInsets(0, 0, 0, 0)) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AppNavHost(navController = navController)

            if (showTabBar && barMode == TabBarMode.Actions) {
                // Tapping anywhere outside the menu closes it.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { barMode = TabBarMode.Rest },
                )
            }

            AnimatedVisibility(
                visible = showTabBar,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                VioraTabBar(
                    items = items,
                    selectedIndex = selectedIndex.coerceAtLeast(0),
                    onItemClick = { index ->
                        // From a plot screen, first close it: back to the root of the tab it came from.
                        if (onPlotScreen) navController.popBackStack(tabs[originIndex].startRoute, inclusive = false)
                        if (!(onPlotScreen && index == originIndex)) {
                            navController.navigate(tabs[index].graph) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    actionLabel = actionLabel,
                    closeLabel = stringResource(R.string.nav_action_close),
                    actions = actions,
                    mode = barMode,
                    onActionClick = {
                        barMode = if (barMode == TabBarMode.Actions) TabBarMode.Rest else TabBarMode.Actions
                    },
                    // The logging screens do not exist yet: picking an option only closes the menu.
                    onActionSelected = { barMode = TabBarMode.Rest },
                    onCollapsedBarClick = { barMode = TabBarMode.Rest },
                    barRowModifier = Modifier.homeTourTarget(HomeTourTarget.Register),
                    modifier = Modifier.navigationBarsPadding().padding(bottom = Spacing.sm),
                )
            }

            if (tourRunning && onHome) {
                HomeTourOverlay(
                    targets = tourTargets,
                    onFinish = {
                        tourRunning = false
                        tourViewModel.complete()
                    },
                )
            }
        }
    }
    }
}

@Composable
private fun recordActions(): List<TabBarAction> = listOf(
    TabBarAction(
        icon = R.drawable.ic_nutrition,
        title = stringResource(R.string.record_sampling_title),
        subtitle = stringResource(R.string.record_sampling_subtitle),
        iconBackground = VioraTabBarActionColors.SamplingBackground,
        iconTint = VioraTabBarActionColors.SamplingTint,
    ),
    TabBarAction(
        icon = R.drawable.ic_content_cut,
        title = stringResource(R.string.record_thinning_title),
        subtitle = stringResource(R.string.record_thinning_subtitle),
        iconBackground = VioraTabBarActionColors.ThinningBackground,
        iconTint = VioraTabBarActionColors.ThinningTint,
    ),
    TabBarAction(
        icon = R.drawable.ic_inventory,
        title = stringResource(R.string.record_harvest_title),
        subtitle = stringResource(R.string.record_harvest_subtitle),
        iconBackground = VioraTabBarActionColors.HarvestBackground,
        iconTint = VioraTabBarActionColors.HarvestTint,
    ),
    TabBarAction(
        icon = R.drawable.ic_edit_note,
        title = stringResource(R.string.record_note_title),
        subtitle = stringResource(R.string.record_note_subtitle),
        iconBackground = VioraTabBarActionColors.NoteBackground,
        iconTint = VioraTabBarActionColors.NoteTint,
    ),
)
