package pe.edu.upc.viora.features.profiles.presentation.state

import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile

data class AccountUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isSignedOut: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val showLanguageSheet: Boolean = false,
    val currentLanguage: String = "es",
    val notificationsEnabled: Boolean = true,
)
