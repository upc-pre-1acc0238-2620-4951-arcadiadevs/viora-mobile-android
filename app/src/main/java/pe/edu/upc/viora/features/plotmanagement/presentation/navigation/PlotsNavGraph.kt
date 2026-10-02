package pe.edu.upc.viora.features.plotmanagement.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import pe.edu.upc.viora.core.navigation.PlotsGraph
import pe.edu.upc.viora.core.navigation.PlotsRoute
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.PlotsScreen

/** The "Lotes" tab. Plot detail, registration and editing will be added as routes of this graph. */
fun NavGraphBuilder.plotsNavGraph() {
    navigation<PlotsGraph>(startDestination = PlotsRoute) {
        composable<PlotsRoute> { PlotsScreen() }
    }
}
