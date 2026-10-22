package pe.edu.upc.viora.features.iam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import pe.edu.upc.viora.core.database.LocalDataOwner
import pe.edu.upc.viora.core.datastore.SessionStore

/** Where the app goes once the splash ends. */
enum class SplashDestination { HOME, WELCOME }

/** Decides, while the splash plays, whether a saved session opens the Home or the welcome. */
@HiltViewModel
class SplashViewModel @Inject constructor(
    sessionStore: SessionStore,
    localDataOwner: LocalDataOwner,
) : ViewModel() {

    /** Null until the saved session has been read. */
    val destination: StateFlow<SplashDestination?> = flow {
        // A session saved by an older version keeps the data on the phone as its own.
        sessionStore.session()?.let { localDataOwner.adopt(it.userId) }
        emit(if (sessionStore.isSignedIn.first()) SplashDestination.HOME else SplashDestination.WELCOME)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
