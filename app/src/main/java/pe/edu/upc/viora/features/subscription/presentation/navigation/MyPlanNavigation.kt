package pe.edu.upc.viora.features.subscription.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.subscription.presentation.ui.MyPlanActions
import pe.edu.upc.viora.features.subscription.presentation.ui.MyPlanScreen

/** P99 · Mi plan, opened from the "Tu plan" card of Mi cuenta. */
@Serializable
data object MyPlanRoute

/** P99 with the destinations of its actions, which the composition root resolves. */
fun NavGraphBuilder.myPlanComposable(actions: MyPlanActions) {
    composable<MyPlanRoute> { MyPlanScreen(actions = actions) }
}
