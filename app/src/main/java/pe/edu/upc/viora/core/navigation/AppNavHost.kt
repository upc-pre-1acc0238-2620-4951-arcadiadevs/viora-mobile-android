package pe.edu.upc.viora.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/** Start destination shown while no bounded context has a screen yet. */
@Serializable
object PlaceholderRoute

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = PlaceholderRoute,
        modifier = modifier
    ) {
        composable<PlaceholderRoute> {
            PlaceholderScreen()
        }
    }
}
