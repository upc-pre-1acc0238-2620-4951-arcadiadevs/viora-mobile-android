package pe.edu.upc.viora.features.profiles.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.features.profiles.presentation.ui.AccountScreen
import pe.edu.upc.viora.features.profiles.presentation.ui.ChangePasswordScreen
import pe.edu.upc.viora.features.profiles.presentation.ui.EditProfileScreen

@Serializable
data object AccountRoute

@Serializable
data object EditProfileRoute

@Serializable
data object ChangePasswordRoute

fun NavGraphBuilder.profilesNavGraph(
    navController: NavController,
    onSignOut: () -> Unit,
) {
    composable<AccountRoute> {
        AccountScreen(
            onBack = { navController.popBackStack() },
            onEditPersonal = { navController.navigate(EditProfileRoute) },
            onChangePassword = { navController.navigate(ChangePasswordRoute) },
            onSignOut = onSignOut,
        )
    }

    composable<EditProfileRoute> {
        EditProfileScreen(
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )
    }

    composable<ChangePasswordRoute> {
        ChangePasswordScreen(
            onBack = { navController.popBackStack() },
            onSuccess = { navController.popBackStack() },
        )
    }
}
