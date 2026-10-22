package pe.edu.upc.viora.features.profiles.presentation.state

import pe.edu.upc.viora.core.locale.AppLanguage
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile

/** P95 · Mi cuenta. */
data class AccountUiState(
    val profile: UserProfile? = null,
    /** Null until the server answers (or offline): the badge only shows a known active access. */
    val isMember: Boolean? = null,
    val showLanguageSheet: Boolean = false,
    /** The language just switched to, for the "Language: English" notice; null when there is none. */
    val languageNotice: AppLanguage? = null,
    val isSigningOut: Boolean = false,
    val isSignedOut: Boolean = false,
)
