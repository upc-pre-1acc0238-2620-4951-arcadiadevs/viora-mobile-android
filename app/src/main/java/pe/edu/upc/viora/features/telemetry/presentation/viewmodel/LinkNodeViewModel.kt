package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.application.usecase.LinkSensorNodeUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.presentation.navigation.SensorsRoute
import pe.edu.upc.viora.features.telemetry.presentation.state.LinkNodeUiState

@HiltViewModel
class LinkNodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val linkSensorNode: LinkSensorNodeUseCase,
) : ViewModel() {

    private val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "SensorsRoute needs a plotId" }

    private val _uiState = MutableStateFlow(LinkNodeUiState())
    val uiState: StateFlow<LinkNodeUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = false, error = null) }
    }

    fun onTypeChange(type: SensorType) {
        _uiState.update {
            it.copy(
                type = type,
                depthCm = if (type == SensorType.SONDA_SUELO) it.depthCm else 30,
            )
        }
    }

    fun onDepthChange(depthCm: Int) {
        _uiState.update { it.copy(depthCm = depthCm) }
    }

    fun reset() {
        _uiState.value = LinkNodeUiState()
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = true) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = linkSensorNode(
                NewSensorNode(
                    plotId = plotId,
                    name = state.name.trim(),
                    type = state.type,
                    depthCm = if (state.type == SensorType.SONDA_SUELO) state.depthCm else null,
                ),
            )
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is AppResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }
}
