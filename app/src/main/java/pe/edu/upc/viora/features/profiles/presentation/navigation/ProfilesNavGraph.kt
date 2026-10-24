package pe.edu.upc.viora.features.profiles.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.profiles.presentation.ui.AccountDeletedScreen
import pe.edu.upc.viora.features.profiles.presentation.ui.AccountScreen
import pe.edu.upc.viora.features.profiles.presentation.ui.DeleteAccountScreen
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

/** P100 · Eliminar cuenta. */
@Serializable
data object DeleteAccountRoute

/** P101 · Cuenta eliminada; [firstName] was read before the profile was forgotten. */
@Serializable
data class AccountDeletedRoute(val firstName: String = "")

/**
 * Mi cuenta and its editions. [onBackHome] returns to the Home (back arrow and "Recorrido del
 * Inicio"); [onSignedOut] leaves the signed-in graph for the sign-in screen. [planSection] is the
 * "Tu plan" card of the subscription feature; [onOpenLogbook] opens the Bitácora (dossiers) before
 * deleting the account, and [onAccountDeleted] leaves P101 for the welcome.
 */
fun NavGraphBuilder.profilesNavGraph(
    navController: NavController,
    onBackHome: () -> Unit,
    onSignedOut: () -> Unit,
    onOpenLogbook: () -> Unit,
    onAccountDeleted: () -> Unit,
    planSection: @Composable () -> Unit,
) {
    composable<AccountRoute> {
        AccountScreen(
            onBack = onBackHome,
            onEditPersonal = { navController.navigate(EditProfileRoute) { launchSingleTop = true } },
            onChangePassword = { navController.navigate(ChangePasswordRoute) { launchSingleTop = true } },
            onReplayHomeTour = onBackHome,
            onSignedOut = onSignedOut,
            onDeleteAccount = { navController.navigate(DeleteAccountRoute) { launchSingleTop = true } },
            planSection = planSection,
        )
    }
    composable<DeleteAccountRoute> {
        DeleteAccountScreen(
            onBack = { navController.popBackStack() },
            onOpenLogbook = onOpenLogbook,
            // Nothing of the account is left: P101 replaces the whole signed-in stack.
            onDeleted = { firstName ->
                navController.navigate(AccountDeletedRoute(firstName)) { popUpTo(navController.graph.id) { inclusive = true } }
            },
        )
    }
    composable<AccountDeletedRoute> { entry ->
        AccountDeletedScreen(firstName = entry.toRoute<AccountDeletedRoute>().firstName, onDone = onAccountDeleted)
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
