package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveGpsFixesUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationService
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsSignal
import pe.edu.upc.viora.features.plotmanagement.presentation.state.GpsReading

/**
 * Turns the stream of GPS fixes into what the walking screen shows. The GPS only runs while
 * [reading] is collected, and a few seconds after the screen goes away it is switched off.
 */
@HiltViewModel
class GpsTrackingViewModel @Inject constructor(
    observeFixes: ObserveGpsFixesUseCase,
    locationService: LocationService,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val reading: StateFlow<GpsReading> = locationService.observeEnabled()
        .distinctUntilChanged()
        .flatMapLatest { enabled ->
            if (!enabled) {
                flowOf(GpsReading.Searching)
            } else {
                observeFixes()
                    .map<GpsFix, GpsFix?> { it }
                    // A missing first fix also times out as a lost signal.
                    .onStart { emit(null) }
                    .transformLatest { fix ->
                        emit(readingOf(fix))
                        delay(SIGNAL_TIMEOUT_MS)
                        emit(GpsReading.Lost(fix))
                    }
                    .catch { emit(GpsReading.Lost(null)) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), GpsReading.Searching)

    private fun readingOf(fix: GpsFix?): GpsReading = when {
        fix == null -> GpsReading.Searching
        fix.signal == GpsSignal.LOST -> GpsReading.Lost(fix)
        else -> GpsReading.Located(fix)
    }

    companion object {
        /** How long without a fix before the GPS is considered lost. */
        const val SIGNAL_TIMEOUT_MS = 10_000L
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
