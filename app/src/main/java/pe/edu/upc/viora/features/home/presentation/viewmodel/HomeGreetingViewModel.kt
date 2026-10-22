package pe.edu.upc.viora.features.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.RefreshProfileUseCase

/** The producer's name for the Home header ("Hola, Teodoro", avatar "TM"); null until the profile is on the phone. */
@HiltViewModel
class HomeGreetingViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    refreshProfile: RefreshProfileUseCase,
) : ViewModel() {

    val fullName: StateFlow<String?> = observeProfile()
        .map { profile -> profile?.fullName?.takeIf { it.isNotBlank() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { refreshProfile() }
    }
}
