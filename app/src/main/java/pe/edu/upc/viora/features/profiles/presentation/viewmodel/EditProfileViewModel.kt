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
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.iam.presentation.state.IamCountry
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileStore
import pe.edu.upc.viora.features.profiles.presentation.state.EditProfileUiState

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val profileStore: ProfileStore,
    private val sessionStore: SessionStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cached = profileStore.getProfile()
            if (cached != null) {
                val country = IamCountry.fromIso(cached.country)
                val digits = cached.phoneNumber.removePrefix(country.dialCode).removePrefix("+")
                _uiState.update {
                    it.copy(
                        fullName = cached.fullName,
                        initialFullName = cached.fullName,
                        country = country,
                        initialCountry = country,
                        phone = digits,
                        initialPhone = digits,
                        email = cached.email.orEmpty(),
                    )
                }
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, fullNameTouched = true) }
    }

    fun onPhoneChange(value: String) {
        val digits = value.filter(Char::isDigit).take(_uiState.value.country.nationalDigits)
        _uiState.update { it.copy(phone = digits, phoneTouched = true) }
    }

    fun onCountryChange(country: IamCountry) {
        _uiState.update {
            it.copy(country = country, phone = it.phone.take(country.nationalDigits))
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        if (!state.canSave || state.isLoading) {
            _uiState.update { it.copy(attemptedSave = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = sessionStore.session()?.userId ?: "current-user"

            val result = if (state.isInitialCompletion) {
                repository.createProfile(
                    fullName = state.fullName.trim(),
                    country = state.country.iso,
                    phoneNumber = state.phone,
                )
            } else {
                repository.updateProfile(
                    userId = userId,
                    fullName = state.fullName.trim(),
                    country = state.country.iso,
                    phoneNumber = state.phone,
                )
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.toString(),
                        )
                    }
                }
            }
        }
    }
}
