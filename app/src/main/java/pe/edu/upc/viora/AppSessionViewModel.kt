package pe.edu.upc.viora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.datastore.SessionEvents
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.features.telemetry.application.usecase.UpdateCriticalAlertScheduleUseCase

/** Lets the app shell react when the session can no longer be renewed. */
@HiltViewModel
class AppSessionViewModel @Inject constructor(
    events: SessionEvents,
    sessionStore: SessionStore,
    updateSchedule: UpdateCriticalAlertScheduleUseCase,
) : ViewModel() {
    val sessionExpired: SharedFlow<Unit> = events.expired

    init {
        viewModelScope.launch {
            sessionStore.isSignedIn.distinctUntilChanged().collect { updateSchedule(it) }
        }
    }
}
