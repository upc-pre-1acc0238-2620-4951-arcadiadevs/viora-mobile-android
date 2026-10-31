package pe.edu.upc.viora.features.croploadregulation.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.core.designsystem.component.AssistantSheetSlot
import pe.edu.upc.viora.core.navigation.LotSection
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.RegisterThinningScreen
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.ThinningPlanScreen
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.ThinningResultScreen

/** P61 · Plan del lote. [campaignYear] 0 means the current campaign; [recordBloom] opens its P64 sheet on arrival. */
@Serializable
data class ThinningPlanRoute(val plotId: String, val plotName: String = "", val campaignYear: Int = 0, val recordBloom: Boolean = false)

/** P62 · Registrar aclareo, against the prescription cached for the plot and campaign. */
@Serializable
data class RegisterThinningRoute(val plotId: String, val plotName: String = "", val campaignYear: Int = 0)

/** P63 · Aclareo registrado. [prescriptionId] lets it download the thinning again when it is not cached. */
@Serializable
data class ThinningResultRoute(
    val plotId: String,
    val prescriptionId: String,
    val plotName: String = "",
    val campaignYear: Int = 0,
)

/**
 * P61 with its P64 sheet, P62 and P63. [onOpenSection] opens another section of the same plot from "Más opciones",
 * [onOpenLogbook] the Bitácora tab and [onOpenAlternation] the Alternancia of the plot; the composition root knows
 * where each one lives.
 */
fun NavGraphBuilder.thinningPlanComposable(
    navController: NavController,
    onOpenSection: (section: LotSection, plotId: String, plotName: String) -> Unit,
    onOpenLogbook: () -> Unit,
    onOpenAlternation: (plotId: String, plotName: String) -> Unit,
    assistantSheet: AssistantSheetSlot? = null,
) {
    composable<ThinningPlanRoute> { entry ->
        val route = entry.toRoute<ThinningPlanRoute>()
        ThinningPlanScreen(
            onBack = { navController.popBackStack() },
            onContinueSampling = { plotName ->
                navController.navigate(SamplingRoundRoute(plotId = route.plotId, plotName = plotName.ifBlank { route.plotName }))
            },
            onRegisterThinning = { plotName, campaignYear ->
                navController.navigate(RegisterThinningRoute(plotId = route.plotId, plotName = plotName, campaignYear = campaignYear))
            },
            onViewResult = { prescriptionId, plotName, campaignYear ->
                navController.navigate(
                    ThinningResultRoute(plotId = route.plotId, prescriptionId = prescriptionId, plotName = plotName, campaignYear = campaignYear),
                )
            },
            onOpenSection = { section, plotName -> onOpenSection(section, route.plotId, plotName) },
            assistantSheet = assistantSheet,
        )
    }
    composable<RegisterThinningRoute> { entry ->
        val route = entry.toRoute<RegisterThinningRoute>()
        RegisterThinningScreen(
            onClose = { navController.popBackStack() },
            // P63 replaces the form, so Back from the result returns to the plan, not to a form already sent.
            onSaved = { prescriptionId ->
                navController.navigate(
                    ThinningResultRoute(
                        plotId = route.plotId,
                        prescriptionId = prescriptionId,
                        plotName = route.plotName,
                        campaignYear = route.campaignYear,
                    ),
                ) { popUpTo<RegisterThinningRoute> { inclusive = true } }
            },
        )
    }
    composable<ThinningResultRoute> { entry ->
        val route = entry.toRoute<ThinningResultRoute>()
        ThinningResultScreen(
            onClose = { navController.popBackStack() },
            onOpenLogbook = onOpenLogbook,
            onOpenAlternation = { plotName -> onOpenAlternation(route.plotId, plotName.ifBlank { route.plotName }) },
        )
    }
}
