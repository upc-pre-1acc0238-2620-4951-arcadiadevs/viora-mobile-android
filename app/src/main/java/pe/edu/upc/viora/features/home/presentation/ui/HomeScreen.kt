package pe.edu.upc.viora.features.home.presentation.ui

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraSectionHeader
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.home.presentation.state.HomeUiState
import pe.edu.upc.viora.features.home.presentation.tour.HomeTourTarget
import pe.edu.upc.viora.features.home.presentation.tour.LocalHomeTourTargets
import pe.edu.upc.viora.features.home.presentation.tour.homeTourTarget
import pe.edu.upc.viora.features.home.presentation.viewmodel.HomeViewModel
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

private val ScreenPadding = 24.dp

/**
 * Home / "Inicio" (Figma P10). The container is here: header, headline, context chips, week and
 * the states (loading, no plots, offline, error). The sections owned by other features plug
 * into [HomeScreenContent]'s `sections` slot, in design order: phase card, "Hoy en tu campo"
 * (weather, alerts, soil moisture) and "Tu alternancia". "Mis lotes" always comes last.
 */
@Composable
fun HomeScreen(
    onRegisterPlot: () -> Unit,
    onOpenPlot: (PlotId) -> Unit,
    onOpenPlots: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenAlternation: (plotId: PlotId, plotName: String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenWeatherForecast: (plotId: PlotId, plotName: String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    HomeScreenContent(
        state = state,
        onRefresh = viewModel::refresh,
        onRegisterPlot = onRegisterPlot,
        onOpenPlot = onOpenPlot,
        onOpenPlots = onOpenPlots,
        onOpenAlerts = onOpenAlerts,
        onOpenAlternation = onOpenAlternation,
        onOpenWeatherForecast = onOpenWeatherForecast,
        onFocusPlot = viewModel::focusPlot,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onRegisterPlot: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenPlot: (PlotId) -> Unit = {},
    onOpenPlots: () -> Unit = {},
    onOpenAlerts: () -> Unit = {},
    onOpenAlternation: (plotId: PlotId, plotName: String) -> Unit = { _, _ -> },
    onOpenWeatherForecast: (plotId: PlotId, plotName: String) -> Unit = { _, _ -> },
    onFocusPlot: (PlotId) -> Unit = {},
    today: LocalDate = LocalDate.now(),
    sections: @Composable ColumnScope.() -> Unit = {},
) {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val plots = (state as? HomeUiState.Content)?.plots.orEmpty()
    val hasNoPlots = state is HomeUiState.NoPlots
    val scrollState = rememberScrollState()
    var pickingFocus by rememberSaveable { mutableStateOf(false) }

    // The tour scrolls the Home from outside to bring each section into view.
    val tourTargets = LocalHomeTourTargets.current
    DisposableEffect(tourTargets, scrollState) {
        tourTargets?.scrollBy = { delta -> scrollState.animateScrollBy(delta) }
        onDispose { tourTargets?.scrollBy = null }
    }

    PullToRefreshBox(
        isRefreshing = (state as? HomeUiState.Content)?.isRefreshing == true,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = systemBars.calculateTopPadding() + 12.dp,
                    bottom = VioraTabBarDefaults.ContentBottomPadding + systemBars.calculateBottomPadding(),
                ),
        ) {
            HomeHeader(
                date = today,
                isOffline = state.isOffline,
                lastRefresh = state.lastRefresh,
                onOpenAlerts = {},
                hasUnreadAlerts = state.activeAlertsCount > 0,
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
            Spacer(Modifier.height(28.dp))
            // Headline, chips, notice and week are one block for the tour: "what is today".
            Column(modifier = Modifier.padding(horizontal = ScreenPadding).homeTourTarget(HomeTourTarget.Today)) {
                HomeHeadline(
                    lead = stringResource(if (hasNoPlots) R.string.home_headline_empty_lead else R.string.home_headline_lead),
                    emphasis = stringResource(if (hasNoPlots) R.string.home_headline_empty_emphasis else R.string.home_headline_emphasis),
                )
                HomeContextChips(
                    campaignYear = Year.from(today).value,
                    plotCount = plots.size,
                    totalHectares = plots.sumOf { it.areaHectares },
                    modifier = Modifier.padding(top = 12.dp),
                    focusedName = (state as? HomeUiState.Content)?.focusedPlot?.name,
                    onChangeFocus = { pickingFocus = true },
                )
                if (state.isOffline) HomeOfflineNotice(modifier = Modifier.padding(top = 16.dp))
                // Without plots the design drops the week and goes straight to the invitation.
                if (!hasNoPlots) HomeWeekStrip(today = today, modifier = Modifier.padding(top = 22.dp))
            }
            if (pickingFocus && state is HomeUiState.Content) {
                FocusPicker(state = state, onSelect = onFocusPlot, onDismiss = { pickingFocus = false })
            }
            when (state) {
                HomeUiState.Loading -> Centered { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                is HomeUiState.Error -> ErrorState(error = state.error, onRetry = onRefresh)
                is HomeUiState.NoPlots -> HomeFirstPlotCard(
                    onRegisterPlot = onRegisterPlot,
                    modifier = Modifier.padding(start = ScreenPadding, end = ScreenPadding, top = 24.dp),
                )
                is HomeUiState.Content -> {
                    sections()
                    HomeTodayInFieldSection(
                        activeAlertsCount = state.activeAlertsCount,
                        onOpenAlerts = onOpenAlerts,
                        onOpenWeatherForecast = {
                            state.focusedPlot?.let { onOpenWeatherForecast(it.id, it.name) }
                        },
                        modifier = Modifier
                            .padding(top = 28.dp)
                            .homeTourTarget(HomeTourTarget.Field),
                    )
                    val focused = state.focusedPlot
                    val alternation = state.alternation
                    if (focused != null && alternation != null) {
                        HomeAlternationCard(
                            plotName = focused.name,
                            alternation = alternation,
                            onOpen = { onOpenAlternation(focused.id, focused.name) },
                            modifier = Modifier
                                .padding(start = ScreenPadding, end = ScreenPadding, top = 32.dp)
                                .homeTourTarget(HomeTourTarget.Alternation),
                        )
                    }
                    Column(modifier = Modifier.padding(top = 32.dp).homeTourTarget(HomeTourTarget.Plots)) {
                        VioraSectionHeader(
                            title = stringResource(R.string.home_my_plots),
                            actionLabel = stringResource(R.string.home_see_all),
                            onAction = onOpenPlots,
                            modifier = Modifier.padding(horizontal = ScreenPadding),
                        )
                        HomePlotsCarousel(plots = state.plots, onOpenPlot = onOpenPlot)
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusPicker(state: HomeUiState.Content, onSelect: (PlotId) -> Unit, onDismiss: () -> Unit) {
    HomeFocusSheet(
        plots = state.plots,
        focusedId = state.focusedPlot?.id,
        onSelect = {
            onSelect(it)
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun ErrorState(error: AppError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = stringResource(R.string.home_loading_error), style = MaterialTheme.typography.headlineSmall)
        Text(text = stringResource(error.messageRes()), style = MaterialTheme.typography.bodyMedium, color = Neutral600)
        TextButton(onClick = onRetry) { Text(stringResource(R.string.plots_retry)) }
    }
}

private fun samplePlot(id: String, name: String, variety: OliveVariety, hectares: Double, treesPerHectare: Int) = Plot(
    id = PlotId(id),
    name = name,
    variety = variety,
    areaHectares = hectares,
    treesPerHectare = treesPerHectare,
    rowSpacingMeters = 7.0,
    treeSpacingMeters = 4.0,
    outline = listOf(GeoPoint(-17.80, -70.00), GeoPoint(-17.80, -69.99), GeoPoint(-17.81, -69.99), GeoPoint(-17.81, -70.00)),
    lastPruningDate = null,
    isActive = true,
    revision = 1,
)

private val previewPlots = listOf(
    samplePlot("1", "La Yarada 02", OliveVariety.SEVILLANA, 2.5, 72),
    samplePlot("2", "Lote Norte", OliveVariety.CRIOLLA, 1.5, 73),
)

private val previewDay: LocalDate = LocalDate.of(2026, 11, 18)

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, heightDp = 800)
@Composable
private fun HomeContentPreview() {
    VioraTheme {
        HomeScreenContent(
            state = HomeUiState.Content(previewPlots, isOffline = false, lastRefresh = Instant.now(), isRefreshing = false),
            onRefresh = {},
            onRegisterPlot = {},
            today = previewDay,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, heightDp = 800)
@Composable
private fun HomeOfflinePreview() {
    VioraTheme {
        HomeScreenContent(
            state = HomeUiState.Content(previewPlots, isOffline = true, lastRefresh = Instant.now().minusSeconds(3 * 3600), isRefreshing = false),
            onRefresh = {},
            onRegisterPlot = {},
            today = previewDay,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, heightDp = 800)
@Composable
private fun HomeNoPlotsPreview() {
    VioraTheme {
        HomeScreenContent(
            state = HomeUiState.NoPlots(isOffline = false, lastRefresh = Instant.now()),
            onRefresh = {},
            onRegisterPlot = {},
            today = previewDay,
        )
    }
}
