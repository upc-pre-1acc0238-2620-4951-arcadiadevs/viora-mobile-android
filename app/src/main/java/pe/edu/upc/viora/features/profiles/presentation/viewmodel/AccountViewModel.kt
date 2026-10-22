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
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileStore
import pe.edu.upc.viora.features.profiles.presentation.state.AccountUiState

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val profileStore: ProfileStore,
    private val sessionStore: SessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profile ->
                _uiState.update { it.copy(profile = profile) }
            }
        }
        viewModelScope.launch {
            profileStore.languageFlow.collect { lang ->
                _uiState.update { it.copy(currentLanguage = lang) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = sessionStore.session()?.userId ?: "current-user"
            repository.getProfile(userId)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun openLanguageSheet() = _uiState.update { it.copy(showLanguageSheet = true) }
    fun closeLanguageSheet() = _uiState.update { it.copy(showLanguageSheet = false) }

    fun selectLanguage(lang: String) {
        viewModelScope.launch {
            profileStore.saveLanguage(lang)
            _uiState.update { it.copy(currentLanguage = lang, showLanguageSheet = false) }
        }
    }

    fun openSignOutDialog() = _uiState.update { it.copy(showSignOutDialog = true) }
    fun closeSignOutDialog() = _uiState.update { it.copy(showSignOutDialog = false) }

    fun confirmSignOut() {
        viewModelScope.launch {
            repository.signOut()
            _uiState.update { it.copy(isSignedOut = true, showSignOutDialog = false) }
        }
    }
}
