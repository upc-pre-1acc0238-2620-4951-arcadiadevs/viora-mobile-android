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
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.features.iam.application.usecase.DeleteAccountUseCase
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.presentation.state.DeleteAccountError
import pe.edu.upc.viora.features.profiles.presentation.state.DeleteAccountUiState
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase

/**
 * P100 · Eliminar cuenta: the producer types the password, ticks "Entiendo", confirms in the sheet, and the account
 * with all its data is deleted on the server and on the phone (then P101).
 */
@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val deleteAccount: DeleteAccountUseCase,
    private val getCurrentSubscription: GetCurrentSubscriptionUseCase,
    observePlots: ObservePlotsUseCase,
    observeProfile: ObserveProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeleteAccountUiState())
    val uiState: StateFlow<DeleteAccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observePlots().collect { plots -> _uiState.update { it.copy(plotCount = plots.count { plot -> plot.isActive }) } }
        }
        viewModelScope.launch {
            observeProfile().collect { profile ->
                val first = profile?.fullName?.trim()?.substringBefore(' ').orEmpty()
                // Once the account is deleted the profile is forgotten; keep the name the farewell needs.
                if (first.isNotEmpty()) _uiState.update { it.copy(firstName = first) }
            }
        }
        viewModelScope.launch {
            val subscription = getCurrentSubscription()
            if (subscription is AppResult.Success) _uiState.update { it.copy(hasActivePlan = subscription.value?.entitlementActive == true) }
        }
    }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun onUnderstoodChange(value: Boolean) = _uiState.update { it.copy(understood = value) }

    /** "Eliminar mi cuenta" opens the last confirmation (the sheet) instead of deleting right away. */
    fun askConfirmation() {
        if (!_uiState.value.canDelete) return
        _uiState.update { it.copy(isConfirming = true, error = null) }
    }

    fun dismissConfirmation() = _uiState.update { it.copy(isConfirming = false) }

    fun confirm() {
        val state = _uiState.value
        if (!state.canDelete) return
        _uiState.update { it.copy(isDeleting = true, isConfirming = false, error = null) }
        viewModelScope.launch {
            when (val result = deleteAccount(state.password)) {
                is AppResult.Success -> _uiState.update { it.copy(isDeleting = false, isDeleted = true) }
                is AppResult.Failure -> _uiState.update { it.copy(isDeleting = false, error = result.error.toDeleteError()) }
            }
        }
    }

    private fun AppError.toDeleteError(): DeleteAccountError = when {
        this is AppError.Validation && code == AuthRepository.WRONG_CURRENT_PASSWORD -> DeleteAccountError.WRONG_PASSWORD
        this is AppError.Forbidden -> DeleteAccountError.PROTECTED_ACCOUNT
        this.isConnectivity -> DeleteAccountError.OFFLINE
        else -> DeleteAccountError.GENERIC
    }
}
