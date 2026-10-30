package pe.edu.upc.viora.features.profiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.core.presentation.PhoneCountry
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.UpdateProfileUseCase
import pe.edu.upc.viora.features.profiles.presentation.state.EditProfileUiState
import pe.edu.upc.viora.features.profiles.presentation.state.SaveProfileError

/** P96 · Datos personales: starts from the kept profile and saves only what the server accepts. */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val observeProfile: ObserveProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Mi cuenta downloads the profile; wait for it if it is not on the phone yet.
            val profile = observeProfile().filterNotNull().first()
            val phone = profile.nationalNumber
            _uiState.update {
                it.copy(
                    fullName = profile.fullName,
                    initialFullName = profile.fullName,
                    country = PhoneCountry.fromIso(profile.country),
                    phone = phone,
                    initialPhone = phone,
                    email = profile.email.orEmpty(),
                )
            }
        }
    }

    fun onFullNameChange(value: String) =
        _uiState.update { it.copy(fullName = value, fullNameTouched = true, error = null) }

    fun onPhoneChange(value: String) {
        val digits = value.filter(Char::isDigit).take(_uiState.value.country.nationalDigits)
        _uiState.update { it.copy(phone = digits, phoneTouched = true, error = null) }
    }

    fun saveChanges() {
        val state = _uiState.value
        if (!state.canSave) {
            _uiState.update { it.copy(attemptedSave = true) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = updateProfile(state.fullName.trim(), state.country.iso, state.phone)
            _uiState.update {
                when (result) {
                    is AppResult.Success -> it.copy(isLoading = false, isSaved = true)
                    is AppResult.Failure -> it.copy(
                        isLoading = false,
                        error = if (result.error.isConnectivity) {
                            SaveProfileError.OFFLINE
                        } else {
                            SaveProfileError.GENERIC
                        },
                    )
                }
            }
        }
    }
}
