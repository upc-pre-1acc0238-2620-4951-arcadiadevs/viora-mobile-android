package pe.edu.upc.viora.features.profiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.locale.AppLanguage
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObservePendingDraftSamplesCountUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePendingSettlementsUseCase
import pe.edu.upc.viora.features.home.application.usecase.RestartHomeTourUseCase
import pe.edu.upc.viora.features.iam.application.usecase.SignOutUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveFieldNotesUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncState
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveAnimatedVoiceUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.RefreshProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.SetAnimatedVoiceUseCase
import pe.edu.upc.viora.features.profiles.presentation.state.AccountUiState
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase

/**
 * P95 · Mi cuenta. The language itself is switched by the screen (it needs the activity); this
 * ViewModel survives the recreation and keeps the notice of the switch.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    private val refreshProfile: RefreshProfileUseCase,
    private val getCurrentSubscription: GetCurrentSubscriptionUseCase,
    private val restartHomeTour: RestartHomeTourUseCase,
    private val signOut: SignOutUseCase,
    observeAnimatedVoice: ObserveAnimatedVoiceUseCase,
    private val setAnimatedVoice: SetAnimatedVoiceUseCase,
    observePendingSamples: ObservePendingDraftSamplesCountUseCase,
    observePendingSettlements: ObservePendingSettlementsUseCase,
    observeFieldNotes: ObserveFieldNotesUseCase,
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
        viewModelScope.launch {
            combine(observePendingSamples(), observePendingSettlements(), observeFieldNotes()) { samples, settlements, notes ->
                samples + settlements.size + notes.count { it.syncState != FieldNoteSyncState.SYNCED }
            }.collect { unsent -> _uiState.update { it.copy(unsentRecords = unsent) } }
        }
        viewModelScope.launch { refreshProfile() }
        viewModelScope.launch {
            val subscription = getCurrentSubscription()
            if (subscription is AppResult.Success) _uiState.update { it.copy(isMember = subscription.value?.entitlementActive == true) }
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

    /** Signs out at once, or first asks when records still waiting to be sent would be lost with the session. */
    fun signOut() {
        if (_uiState.value.unsentRecords > 0) {
            _uiState.update { it.copy(confirmingSignOut = true) }
            return
        }
        confirmSignOut()
    }

    fun dismissSignOut() = _uiState.update { it.copy(confirmingSignOut = false) }

    /** Signing out forgets the session and every record of the account on the phone. */
    fun confirmSignOut() {
        if (_uiState.value.isSigningOut) return
        _uiState.update { it.copy(isSigningOut = true, confirmingSignOut = false) }
        viewModelScope.launch {
            signOut.invoke()
            _uiState.update { it.copy(isSigningOut = false, isSignedOut = true) }
        }
    }
}
