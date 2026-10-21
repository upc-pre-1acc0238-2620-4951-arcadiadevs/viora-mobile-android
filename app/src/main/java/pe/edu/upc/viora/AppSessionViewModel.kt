package pe.edu.upc.viora

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow
import pe.edu.upc.viora.core.datastore.SessionEvents

/** Lets the app shell react when the session can no longer be renewed. */
@HiltViewModel
class AppSessionViewModel @Inject constructor(events: SessionEvents) : ViewModel() {
    val sessionExpired: SharedFlow<Unit> = events.expired
}
