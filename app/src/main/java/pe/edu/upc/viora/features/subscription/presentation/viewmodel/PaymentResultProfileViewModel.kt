package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.RefreshProfileUseCase

/** Resolves the signed-in profile without embedding identity in the payment screen. */
@HiltViewModel
class PaymentResultProfileViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    refreshProfile: RefreshProfileUseCase,
) : ViewModel() {
    val name = observeProfile().map { it?.fullName?.trim()?.substringBefore(' ') }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init { viewModelScope.launch { refreshProfile() } }
}
