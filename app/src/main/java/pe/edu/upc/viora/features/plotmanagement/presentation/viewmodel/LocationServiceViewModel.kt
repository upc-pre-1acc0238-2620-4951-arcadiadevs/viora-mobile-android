package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationService

@HiltViewModel
class LocationServiceViewModel @Inject constructor(
    private val service: LocationService,
) : ViewModel() {
    private val mutableEnabled = MutableStateFlow(service.isEnabled())
    val enabled = mutableEnabled.asStateFlow()

    init {
        viewModelScope.launch { service.observeEnabled().collect { mutableEnabled.value = it } }
    }

    fun refresh() { mutableEnabled.value = service.isEnabled() }
}
