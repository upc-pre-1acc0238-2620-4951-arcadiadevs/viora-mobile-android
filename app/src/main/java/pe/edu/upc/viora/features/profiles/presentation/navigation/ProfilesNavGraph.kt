package pe.edu.upc.viora.features.profiles.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.profiles.presentation.ui.AccountScreen
import pe.edu.upc.viora.features.profiles.presentation.ui.ChangePasswordScreen
import pe.edu.upc.viora.features.profiles.presentation.ui.EditProfileScreen

/** P95 · Mi cuenta, opened from the avatar of the Home. */
@Serializable
data object AccountRoute

/** P96 · Datos personales. */
@Serializable
data object EditProfileRoute

/** P97 · Cambiar contraseña. */
@Serializable
data object ChangePasswordRoute

/**
 * Mi cuenta and its editions. [onBackHome] returns to the Home (back arrow and "Recorrido del
 * Inicio"); [onSignedOut] leaves the signed-in graph for the sign-in screen.
 */
fun NavGraphBuilder.profilesNavGraph(
    navController: NavController,
    onBackHome: () -> Unit,
    onSignedOut: () -> Unit,
) {
    composable<AccountRoute> {
        AccountScreen(
            onBack = onBackHome,
            onEditPersonal = { navController.navigate(EditProfileRoute) { launchSingleTop = true } },
            onChangePassword = { navController.navigate(ChangePasswordRoute) { launchSingleTop = true } },
            onReplayHomeTour = onBackHome,
            onSignedOut = onSignedOut,
        )
    }
    composable<EditProfileRoute> {
        EditProfileScreen(
            onClose = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )
    }
    composable<ChangePasswordRoute> {
        ChangePasswordScreen(
            onClose = { navController.popBackStack() },
            onChanged = { navController.popBackStack() },
        )
    }
}
