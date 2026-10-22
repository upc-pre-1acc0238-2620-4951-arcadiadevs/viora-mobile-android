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
import pe.edu.upc.viora.features.profiles.application.usecase.ChangePasswordUseCase
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.presentation.state.ChangePasswordError
import pe.edu.upc.viora.features.profiles.presentation.state.ChangePasswordUiState

/** P97 · Cambiar contraseña. On success the phone keeps its session with the new tokens. */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePassword: ChangePasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentPasswordChange(value: String) =
        _uiState.update { it.copy(currentPassword = value, currentPasswordIncorrect = false, error = null) }

    fun onNewPasswordChange(value: String) =
        _uiState.update { it.copy(newPassword = value, newPasswordRejection = null, error = null) }

    fun onConfirmPasswordChange(value: String) = _uiState.update { it.copy(confirmPassword = value, error = null) }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = changePassword(state.currentPassword, state.newPassword)
            _uiState.update {
                when (result) {
                    is AppResult.Success -> it.copy(isLoading = false, isChanged = true)
                    is AppResult.Failure -> failed(it, result.error)
                }
            }
        }
    }

    private fun failed(state: ChangePasswordUiState, error: AppError): ChangePasswordUiState {
        val idle = state.copy(isLoading = false)
        return when {
            error is AppError.Validation && error.code == ProfileRepository.WRONG_CURRENT_PASSWORD ->
                idle.copy(currentPasswordIncorrect = true)
            // The local rules passed, so the server refused it for one of its own (e.g. a recent password).
            error is AppError.Validation -> idle.copy(newPasswordRejection = error.detail)
            error is AppError.Offline || error is AppError.Timeout -> idle.copy(error = ChangePasswordError.OFFLINE)
            else -> idle.copy(error = ChangePasswordError.GENERIC)
        }
    }
}
