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
import pe.edu.upc.viora.features.home.application.usecase.CompleteHomeTourUseCase
import pe.edu.upc.viora.features.home.application.usecase.ObserveHomeTourSeenUseCase

/**
 * Whether the Home tour should be offered. It is shared by the whole window (the tour dims the
 * tab bar too), so it is created once at the app level.
 */
@HiltViewModel
class HomeTourViewModel @Inject constructor(
    observeSeen: ObserveHomeTourSeenUseCase,
    private val completeTour: CompleteHomeTourUseCase,
) : ViewModel() {

    /** `null` until the stored flag is read, so the tour never flashes for someone who saw it. */
    val isPending: StateFlow<Boolean?> = observeSeen()
        .map { seen -> !seen }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun complete() {
        viewModelScope.launch { completeTour() }
    }
}
