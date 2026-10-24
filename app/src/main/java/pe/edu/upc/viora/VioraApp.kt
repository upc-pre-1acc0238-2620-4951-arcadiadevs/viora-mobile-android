package pe.edu.upc.viora

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import pe.edu.upc.viora.core.designsystem.component.LocalVioraVoiceAnimated
import pe.edu.upc.viora.core.designsystem.component.TabBarAction
import pe.edu.upc.viora.core.designsystem.component.TabBarItem
import pe.edu.upc.viora.core.designsystem.component.TabBarMode
import pe.edu.upc.viora.core.designsystem.component.VioraTabBar
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarActionColors
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.navigation.AuthRoute
import pe.edu.upc.viora.core.navigation.HomeGraph
import pe.edu.upc.viora.core.navigation.HomeRoute
import pe.edu.upc.viora.core.navigation.LogbookGraph
import pe.edu.upc.viora.core.navigation.TopLevelDestination
import pe.edu.upc.viora.core.navigation.WelcomeRoute
import pe.edu.upc.viora.features.croploadregulation.presentation.navigation.RegisterThinningRoute
import pe.edu.upc.viora.features.croploadregulation.presentation.navigation.SelectPlotSamplingRoute
import pe.edu.upc.viora.features.croploadregulation.presentation.navigation.ThinningPlanRoute
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.ThinningPlotPickerSheet
import pe.edu.upc.viora.features.harvestsettlement.presentation.navigation.SettleHarvestRoute
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.HarvestPlotPickerSheet
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.HarvestEntryViewModel
import pe.edu.upc.viora.features.home.domain.valueobject.HomeTour
import pe.edu.upc.viora.features.home.presentation.tour.HomeTourOverlay
import pe.edu.upc.viora.features.home.presentation.tour.HomeTourTarget
import pe.edu.upc.viora.features.home.presentation.tour.HomeTourTargets
import pe.edu.upc.viora.features.home.presentation.tour.LocalHomeTourTargets
import pe.edu.upc.viora.features.home.presentation.tour.homeTourTarget
import pe.edu.upc.viora.features.home.presentation.viewmodel.HomeTourViewModel
import pe.edu.upc.viora.features.phenology.presentation.navigation.HarvestHistoryRoute
import pe.edu.upc.viora.features.phenology.presentation.navigation.WinterChillRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.PlotDetailRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.RegisterPlotRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.NewFieldNoteSheet
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.VoicePreferenceViewModel
import pe.edu.upc.viora.features.telemetry.presentation.navigation.PlotClimateRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.TelemetryDetailRoute
import pe.edu.upc.viora.navigation.AppNavHost

/** App shell: navigation host plus the producer's floating tab bar on top-level screens. */
@Composable
fun VioraApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    // An expired session sends the producer to sign in, with the welcome below so back still works.
    val sessionViewModel: AppSessionViewModel = hiltViewModel()
    LaunchedEffect(navController) {
        sessionViewModel.sessionExpired.collect {
            navController.navigate(WelcomeRoute) { popUpTo(navController.graph.id) { inclusive = true } }
            navController.navigate(AuthRoute())
        }
    }

    val tabs = TopLevelDestination.entries
    val tourTargets = remember { HomeTourTargets() }
    val tourViewModel: HomeTourViewModel = hiltViewModel()
    val voicePreference: VoicePreferenceViewModel = hiltViewModel()
    val animatedVoice by voicePreference.animatedVoice.collectAsStateWithLifecycle()
    val pendingTours by tourViewModel.pendingTours.collectAsStateWithLifecycle()
    var runningTour by remember { mutableStateOf<HomeTour?>(null) }
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
        currentDestination?.hasRoute<PlotClimateRoute>() == true ||
        currentDestination?.hasRoute<TelemetryDetailRoute>() == true ||
        currentDestination?.hasRoute<HarvestHistoryRoute>() == true ||
        currentDestination?.hasRoute<WinterChillRoute>() == true ||
        currentDestination?.hasRoute<ThinningPlanRoute>() == true

    val items = tabs.map { TabBarItem(icon = it.icon, label = stringResource(it.label)) }
    val actionLabel = stringResource(R.string.nav_action_add)
    val actions = recordActions()

    var barMode by remember { mutableStateOf(TabBarMode.Rest) }
    // The sheet the "+" menu opened (thinning, harvest or note), and a counter that shows the
    // "Nota guardada" pill again every time a note is saved.
    var recordSheet by rememberSaveable { mutableStateOf<RecordSheet?>(null) }
    var noteToastKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(selectedIndex, showTabBar) { barMode = TabBarMode.Rest }
    BackHandler(enabled = barMode == TabBarMode.Actions) { barMode = TabBarMode.Rest }

    // An account without plots gets the short tour around "Primer paso"; the full one opens the
    // first time the Home shows a plots section. Each one only once per account.
    val onHome = currentDestination?.hasRoute(HomeRoute::class) == true
    val onScreen = tourTargets.rects.keys
    val tourToOffer = when {
        HomeTourTarget.Register !in onScreen -> null
        HomeTourTarget.Today in onScreen && HomeTourTarget.Plots in onScreen -> HomeTour.FULL
        HomeTourTarget.FirstPlot in onScreen -> HomeTour.FIRST_STEPS
        else -> null
    }
    LaunchedEffect(pendingTours, onHome, tourToOffer, barMode) {
        if (runningTour == null && onHome && barMode == TabBarMode.Rest &&
            tourToOffer != null && pendingTours?.contains(tourToOffer) == true
        ) {
            runningTour = tourToOffer
        }
    }

    // No insets here: each screen decides how to use the area under the system bars (maps go
    // beneath them, lists pad themselves), so the status bar can stay transparent.
    CompositionLocalProvider(LocalHomeTourTargets provides tourTargets, LocalVioraVoiceAnimated provides animatedVoice) {
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
                                // The Home graph is the root once signed in (the splash and the IAM screens are gone).
                                popUpTo<HomeGraph> { saveState = true }
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
                    onActionSelected = { index ->
                        barMode = TabBarMode.Rest
                        when (index) {
                            0 -> navController.navigate(SelectPlotSamplingRoute)
                            1 -> recordSheet = RecordSheet.THINNING
                            2 -> recordSheet = RecordSheet.HARVEST
                            3 -> recordSheet = RecordSheet.NOTE
                        }
                    },
                    onCollapsedBarClick = { barMode = TabBarMode.Rest },
                    barRowModifier = Modifier.homeTourTarget(HomeTourTarget.Register),
                    modifier = Modifier.navigationBarsPadding().padding(bottom = Spacing.sm),
                )
            }

            RecordSheets(
                sheet = recordSheet,
                navController = navController,
                onDismiss = { recordSheet = null },
                onNoteSaved = {
                    recordSheet = null
                    navController.navigate(LogbookGraph) {
                        popUpTo<HomeGraph> { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    noteToastKey++
                },
            )
            SavedNoteToast(key = noteToastKey, modifier = Modifier.align(Alignment.TopCenter))

            val tour = runningTour
            if (tour != null && onHome) {
                HomeTourOverlay(
                    targets = tourTargets,
                    tour = tour,
                    onFinish = { accepted ->
                        runningTour = null
                        tourViewModel.complete(tour)
                        // "Registrar mi primer lote" closes the short tour and opens the plot wizard.
                        if (accepted && tour == HomeTour.FIRST_STEPS) navController.navigate(RegisterPlotRoute)
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

/** The sheets the "+" menu opens over the current screen. */
private enum class RecordSheet { THINNING, HARVEST, NOTE }

@Composable
private fun RecordSheets(
    sheet: RecordSheet?,
    navController: NavHostController,
    onDismiss: () -> Unit,
    onNoteSaved: () -> Unit,
) {
    when (sheet) {
        RecordSheet.THINNING -> ThinningPlotPickerSheet(
            onRegister = { plotId, plotName, year ->
                onDismiss()
                navController.navigate(RegisterThinningRoute(plotId = plotId, plotName = plotName, campaignYear = year))
            },
            onOpenPlan = { plotId, plotName, year ->
                onDismiss()
                navController.navigate(ThinningPlanRoute(plotId = plotId, plotName = plotName, campaignYear = year))
            },
            onDismiss = onDismiss,
        )
        RecordSheet.HARVEST -> {
            val harvest: HarvestEntryViewModel = hiltViewModel()
            val state by harvest.uiState.collectAsStateWithLifecycle()
            HarvestPlotPickerSheet(
                state = state,
                onSelect = { plot ->
                    onDismiss()
                    navController.navigate(SettleHarvestRoute(plotId = plot.plotId, campaignYear = state.campaignYear))
                },
                onDismiss = onDismiss,
            )
        }
        RecordSheet.NOTE -> NewFieldNoteSheet(onSaved = { onNoteSaved() }, onDismiss = onDismiss)
        null -> Unit
    }
}

/** "Nota guardada en tu Bitácora" (Figma P50 · Nota guardada): a pill at the top for a few seconds. */
@Composable
private fun SavedNoteToast(key: Int, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(key) {
        if (key == 0) return@LaunchedEffect
        visible = true
        delay(TOAST_MILLIS)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.statusBarsPadding().padding(top = 8.dp),
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Green900)
                .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Harvest300), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Green900, modifier = Modifier.size(16.dp))
            }
            Text(stringResource(R.string.note_saved_toast), style = MaterialTheme.typography.labelLarge, color = Neutral50)
        }
    }
}

private const val TOAST_MILLIS = 2_500L
