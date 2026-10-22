package pe.edu.upc.viora.features.profiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.locale.AppLanguage
import pe.edu.upc.viora.features.home.application.usecase.RestartHomeTourUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.CheckMembershipUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveAnimatedVoiceUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.RefreshProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.SetAnimatedVoiceUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.SignOutUseCase
import pe.edu.upc.viora.features.profiles.presentation.state.AccountUiState

/**
 * P95 · Mi cuenta. The language itself is switched by the screen (it needs the activity); this
 * ViewModel survives the recreation and keeps the notice of the switch.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    private val refreshProfile: RefreshProfileUseCase,
    private val checkMembership: CheckMembershipUseCase,
    private val restartHomeTour: RestartHomeTourUseCase,
    private val signOut: SignOutUseCase,
    observeAnimatedVoice: ObserveAnimatedVoiceUseCase,
    private val setAnimatedVoice: SetAnimatedVoiceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeProfile().collect { profile -> _uiState.update { it.copy(profile = profile) } }
        }
        viewModelScope.launch {
            observeAnimatedVoice().collect { on -> _uiState.update { it.copy(animatedVoice = on) } }
        }
        viewModelScope.launch { refreshProfile() }
        viewModelScope.launch {
            val membership = checkMembership()
            if (membership is AppResult.Success) _uiState.update { it.copy(isMember = membership.value) }
        }
    }

    fun openLanguageSheet() = _uiState.update { it.copy(showLanguageSheet = true) }

    fun closeLanguageSheet() = _uiState.update { it.copy(showLanguageSheet = false) }

    /** Called right before the screen applies [language]; the notice shows once the screen is back. */
    fun onLanguageChosen(language: AppLanguage) =
        _uiState.update { it.copy(showLanguageSheet = false, languageNotice = language) }

    fun dismissLanguageNotice() = _uiState.update { it.copy(languageNotice = null) }

    /** Voz de Viora animada: whether fixed texts are written letter by letter. */
    fun onAnimatedVoiceChange(enabled: Boolean) {
        viewModelScope.launch { setAnimatedVoice(enabled) }
    }

    /** The tour shows again on the next visit to the Home (Recorrido del Inicio · Ver de nuevo). */
    fun replayHomeTour(then: () -> Unit) {
        viewModelScope.launch {
            restartHomeTour()
            then()
        }
    }

    fun signOut() {
        if (_uiState.value.isSigningOut) return
        _uiState.update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            signOut.invoke()
            _uiState.update { it.copy(isSigningOut = false, isSignedOut = true) }
        }
    }
}
