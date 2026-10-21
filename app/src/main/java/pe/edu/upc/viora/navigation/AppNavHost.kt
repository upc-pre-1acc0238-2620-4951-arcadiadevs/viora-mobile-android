package pe.edu.upc.viora.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import pe.edu.upc.viora.BuildConfig
import androidx.navigation.navDeepLink
import pe.edu.upc.viora.core.navigation.AuthRoute
import pe.edu.upc.viora.core.navigation.OnboardingRoute
import pe.edu.upc.viora.core.navigation.SplashRoute
import pe.edu.upc.viora.core.navigation.WelcomeRoute
import pe.edu.upc.viora.core.navigation.PASSWORD_RESET_LINK_BASE
import pe.edu.upc.viora.core.navigation.ResetPasswordRoute
import pe.edu.upc.viora.core.navigation.HomeGraph
import pe.edu.upc.viora.core.navigation.HomeRoute
import pe.edu.upc.viora.core.navigation.LogbookGraph
import pe.edu.upc.viora.core.navigation.LogbookRoute
import pe.edu.upc.viora.core.navigation.PlanGraph
import pe.edu.upc.viora.core.navigation.PlanRoute
import pe.edu.upc.viora.core.navigation.PlotsGraph
import pe.edu.upc.viora.features.croploadregulation.presentation.navigation.SamplingRoundRoute
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.CampaignPlanScreen
import pe.edu.upc.viora.features.croploadregulation.presentation.navigation.ThinningPlanRoute
import pe.edu.upc.viora.features.croploadregulation.presentation.navigation.thinningPlanComposable
import pe.edu.upc.viora.features.croploadregulation.presentation.navigation.samplingNavGraph
import pe.edu.upc.viora.features.harvestsettlement.presentation.navigation.AgronomicDossierRoute
import pe.edu.upc.viora.features.harvestsettlement.presentation.navigation.CampaignClosedRoute
import pe.edu.upc.viora.features.harvestsettlement.presentation.navigation.agronomicDossierComposable
import pe.edu.upc.viora.features.harvestsettlement.presentation.navigation.SettleHarvestRoute
import pe.edu.upc.viora.features.harvestsettlement.presentation.navigation.campaignClosedComposable
import pe.edu.upc.viora.features.harvestsettlement.presentation.navigation.settleHarvestComposable
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.HarvestEntrySection
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.LogbookScreen
import pe.edu.upc.viora.features.home.presentation.ui.HomeScreen
import pe.edu.upc.viora.features.iam.presentation.ui.IamScreen
import pe.edu.upc.viora.features.iam.presentation.ui.OnboardingScreen
import pe.edu.upc.viora.features.iam.presentation.ui.SplashScreen
import pe.edu.upc.viora.features.iam.presentation.ui.WelcomeScreen
import pe.edu.upc.viora.features.iam.presentation.viewmodel.SplashDestination
import pe.edu.upc.viora.features.phenology.presentation.navigation.HarvestHistoryRoute
import pe.edu.upc.viora.features.phenology.presentation.navigation.WinterChillRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.PlotDetailRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.RegisterPlotRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.plotScreens
import pe.edu.upc.viora.features.plotmanagement.presentation.navigation.plotsNavGraph
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSection
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.PlotSilhouetteById
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric
import pe.edu.upc.viora.features.subscription.presentation.navigation.CooperativeCodeGraph
import pe.edu.upc.viora.features.subscription.presentation.navigation.RedeemCodeRoute
import pe.edu.upc.viora.features.subscription.presentation.navigation.cooperativeCodeNavGraph
import pe.edu.upc.viora.features.telemetry.presentation.navigation.AlertsCenterRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.PlotClimateRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.TelemetryDetailRoute
import pe.edu.upc.viora.features.telemetry.presentation.navigation.alertsNavGraph
import pe.edu.upc.viora.features.telemetry.presentation.navigation.climateNavGraph

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
        startDestination = debugStartDestination() ?: SplashRoute,
        modifier = modifier,
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onFinished = { destination ->
                    val next: Any = if (destination == SplashDestination.HOME) HomeGraph else WelcomeRoute
                    navController.navigate(next) { popUpTo<SplashRoute> { inclusive = true } }
                },
            )
        }
        composable<WelcomeRoute> {
            WelcomeScreen(
                onStartOnboarding = { navController.navigate(OnboardingRoute) },
                onSignIn = { navController.navigate(AuthRoute()) },
            )
        }
        composable<OnboardingRoute> {
            OnboardingScreen(
                onCreateAccount = { navController.navigate(AuthRoute(createAccount = true)) },
                onExit = { navController.popBackStack() },
            )
        }
        composable<AuthRoute> {
            IamScreen(
                onAuthenticated = { cooperativeCode ->
                    if (cooperativeCode == null) navController.openHome() else navController.redeemSavedCode(cooperativeCode)
                },
                onExit = { navController.popBackStack() },
                onStartOnboarding = { navController.navigate(OnboardingRoute) { launchSingleTop = true } },
            )
        }
        // The recovery link opens the app here. After saving the new password the producer signs in
        // again; leaving the flow goes to the welcome, since nothing is below it.
        composable<ResetPasswordRoute>(
            deepLinks = listOf(navDeepLink<ResetPasswordRoute>(basePath = PASSWORD_RESET_LINK_BASE)),
        ) {
            IamScreen(
                onAuthenticated = { navController.openHome() },
                onExit = { navController.navigate(WelcomeRoute) { popUpTo(navController.graph.id) { inclusive = true } } },
                onStartOnboarding = { navController.navigate(OnboardingRoute) { launchSingleTop = true } },
            )
        }
        navigation<HomeGraph>(startDestination = HomeRoute) {
            composable<HomeRoute> {
                HomeScreen(
                    onRegisterPlot = { navController.navigate(RegisterPlotRoute) },
                    onOpenPlot = { id -> navController.navigate(PlotDetailRoute(plotId = id.value)) },
                    onOpenAlternation = { id, name -> navController.navigate(HarvestHistoryRoute(plotId = id.value, plotName = name)) },
                    onOpenPlots = {
                        navController.navigate(PlotsGraph) {
                            popUpTo<HomeGraph> { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenAlerts = { navController.navigate(AlertsCenterRoute()) },
                    onOpenWeatherForecast = { id, name ->
                        navController.navigate(PlotClimateRoute(plotId = id.value, plotName = name))
                    },
                    onOpenSoilMoisture = { id, name ->
                        navController.navigate(
                            TelemetryDetailRoute(plotId = id.value, plotName = name, metric = TelemetryMetric.SOIL_MOISTURE.name),
                        )
                    },
                    sections = {
                        HarvestEntrySection(
                            onSettle = { plotId, year -> navController.navigate(SettleHarvestRoute(plotId = plotId, campaignYear = year)) },
                        )
                    },
                )
            }
        }
        // The form is replaced by the P73 receipt (inclusive pop), so Back never returns to it.
        settleHarvestComposable(
            navController = navController,
            onSettled = { plotId, year -> navController.openCampaignReceipt(plotId, year, popForm = true) },
            onQueued = { plotId, year -> navController.openCampaignReceipt(plotId, year, popForm = true) },
            onViewReceipt = { plotId, year -> navController.openCampaignReceipt(plotId, year, popForm = true) },
        )
        campaignClosedComposable(
            navController = navController,
            // Back from P76 returns where "Listo" of P73 would (Figma prototype: P76 Atrás -> P50 Bitácora).
            onOpenDossier = { plotId, plotName, year ->
                navController.navigate(AgronomicDossierRoute(plotId = plotId, plotName = plotName, campaignYear = year)) {
                    popUpTo<CampaignClosedRoute> { inclusive = true }
                }
            },
            onOpenAlternation = { plotId, plotName ->
                navController.navigate(HarvestHistoryRoute(plotId = plotId, plotName = plotName))
            },
            onCorrect = { plotId, year ->
                navController.navigate(SettleHarvestRoute(plotId = plotId, campaignYear = year)) { popUpTo<CampaignClosedRoute> { inclusive = true } }
            },
        )
        agronomicDossierComposable(navController)
        plotsNavGraph(navController)
        cooperativeCodeNavGraph(
            navController = navController,
            onLeave = { navController.openHome() },
            onRegisterPlot = {
                navController.openHome()
                navController.navigate(RegisterPlotRoute)
            },
        )
        plotScreens(navController)
        climateNavGraph(navController)
        alertsNavGraph(
            navController = navController,
            onOpenPlot = { plotId -> navController.navigate(PlotDetailRoute(plotId = plotId)) },
            onOpenWinterChill = { plotId, plotName -> navController.navigate(WinterChillRoute(plotId = plotId, plotName = plotName)) },
        )
        samplingNavGraph(
            navController = navController,
            plotSilhouetteSlot = { plotId, slotModifier ->
                PlotSilhouetteById(plotId = plotId, modifier = slotModifier, showTreeGrid = true)
            },
        )
        thinningPlanComposable(
            navController = navController,
            onOpenSection = { section, plotId, plotName -> navController.openLotSection(section, plotId, plotName) },
            onOpenLogbook = {
                navController.navigate(LogbookGraph) {
                    popUpTo<HomeGraph> { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onOpenAlternation = { plotId, plotName -> navController.navigate(HarvestHistoryRoute(plotId = plotId, plotName = plotName)) },
        )
        navigation<PlanGraph>(startDestination = PlanRoute) {
            composable<PlanRoute> {
                CampaignPlanScreen(
                    onOpenPlan = { plotId, plotName, year ->
                        navController.navigate(ThinningPlanRoute(plotId = plotId, plotName = plotName, campaignYear = year))
                    },
                    onOpenAlerts = { navController.navigate(AlertsCenterRoute()) },
                    onRegisterPlot = { navController.navigate(RegisterPlotRoute) },
                )
            }
        }
        navigation<LogbookGraph>(startDestination = LogbookRoute) {
            composable<LogbookRoute> {
                LogbookScreen(
                    onOpenSettlement = { plotId, year -> navController.openCampaignReceipt(plotId, year, popForm = false) },
                    onOpenAlerts = { navController.navigate(AlertsCenterRoute()) },
                    onContinueSampling = { plotId, plotName ->
                        navController.navigate(
                            SamplingRoundRoute(
                                plotId = plotId,
                                plotName = plotName,
                            ),
                        )
                    },
                )
            }
        }
    }
}

/** Opens another section of a plot from its "Más opciones" sheet. */
private fun NavHostController.openLotSection(section: LotSection, plotId: String, plotName: String) {
    when (section) {
        LotSection.HARVEST -> navigate(HarvestHistoryRoute(plotId = plotId, plotName = plotName))
        LotSection.WINTER_CHILL -> navigate(WinterChillRoute(plotId = plotId, plotName = plotName))
        LotSection.THINNING -> navigate(ThinningPlanRoute(plotId = plotId, plotName = plotName))
        LotSection.CLIMATE -> navigate(PlotClimateRoute(plotId = plotId, plotName = plotName))
        LotSection.SENSORS -> navigate(SensorsRoute(plotId = plotId, plotName = plotName))
        LotSection.DOSSIER -> navigate(AgronomicDossierRoute(plotId = plotId, plotName = plotName))
    }
}

/** Opens the P73 receipt of a campaign; with [popForm] the settle form leaves the back stack. */
private fun NavHostController.openCampaignReceipt(plotId: String, year: Int, popForm: Boolean) {
    navigate(CampaignClosedRoute(plotId = plotId, campaignYear = year)) {
        if (popForm) popUpTo<SettleHarvestRoute> { inclusive = true }
    }
}

/**
 * Debug builds may start on a screen directly, without going through the sign-up that opens it:
 * `viora.debugStartScreen=cooperative-code` in `local.properties`. Release builds always start on the splash.
 */
private fun debugStartDestination(): Any? = when (BuildConfig.DEBUG_START_SCREEN) {
    "cooperative-code" -> CooperativeCodeGraph
    else -> null
}

/** Opens the Home as the only screen: nothing of the splash or the IAM flow stays below it. */
private fun NavHostController.openHome() {
    navigate(HomeGraph) { popUpTo(graph.id) { inclusive = true } }
}

/** After the email is verified, P05 redeems the code saved in T02c (F04); the IAM flow does not stay below. */
private fun NavHostController.redeemSavedCode(code: String) {
    navigate(RedeemCodeRoute(code = code)) { popUpTo(graph.id) { inclusive = true } }
}
