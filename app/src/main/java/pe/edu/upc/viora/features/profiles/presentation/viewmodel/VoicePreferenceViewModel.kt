package pe.edu.upc.viora.features.profiles.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveAnimatedVoiceUseCase

/**
 * The "Voz de Viora animada" preference for the whole window: the app root provides it to every
 * `VioraVoice`, so the screens do not have to pass it along.
 */
@HiltViewModel
class VoicePreferenceViewModel @Inject constructor(
    observeAnimatedVoice: ObserveAnimatedVoiceUseCase,
) : ViewModel() {

    val animatedVoice: StateFlow<Boolean> = observeAnimatedVoice()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
}
