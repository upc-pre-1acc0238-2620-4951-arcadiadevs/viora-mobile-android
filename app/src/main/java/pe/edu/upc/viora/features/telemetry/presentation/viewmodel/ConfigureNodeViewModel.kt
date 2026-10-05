package pe.edu.upc.viora.features.telemetry.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.telemetry.application.usecase.ObserveSensorNodesUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.UnlinkSensorNodeUseCase
import pe.edu.upc.viora.features.telemetry.application.usecase.UpdateSensorNodeUseCase
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorStatus
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.presentation.state.ConfigureNodeUiState

@HiltViewModel
class ConfigureNodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeNodes: ObserveSensorNodesUseCase,
    observePlot: ObservePlotUseCase,
    private val updateNode: UpdateSensorNodeUseCase,
    private val unlinkNode: UnlinkSensorNodeUseCase,
) : ViewModel() {

    private val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId"))
    private val nodeId: String = checkNotNull(savedStateHandle.get<String>("nodeId"))

    private val _uiState = MutableStateFlow(ConfigureNodeUiState())
    val uiState: StateFlow<ConfigureNodeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observePlot(PlotId(plotId)).collectLatest { plot ->
                if (!plot?.name.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(plotName = plot?.name.orEmpty())
                }
            }
        }
        viewModelScope.launch {
            observeNodes(plotId).collectLatest { nodes ->
                val node = nodes.firstOrNull { it.id == nodeId }
                if (node == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@collectLatest
                }
                val current = _uiState.value
                if (current.node?.id != node.id || (!current.isSaving && !current.isUnlinking)) {
                    _uiState.value = current.copy(
                        node = node,
                        nodeCount = nodes.size,
                        name = if (current.node?.id == node.id) current.name else node.name,
                        depthCm = if (current.node?.id == node.id) current.depthCm else node.depthCm ?: 30,
                        transmitReadings = if (current.node?.id == node.id) current.transmitReadings else node.status == SensorStatus.ACTIVE,
                        isLoading = false,
                    )
                } else {
                    _uiState.value = current.copy(nodeCount = nodes.size)
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, nameError = false, error = null)
    }

    fun onDepthChange(value: Int) {
        _uiState.value = _uiState.value.copy(
            depthCm = value,
            depthError = value !in setOf(30, 60),
            error = null,
        )
    }

    fun onTransmitReadingsChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(transmitReadings = value, error = null)
    }

    fun save(onSuccess: () -> Unit) {
        val state = _uiState.value
        val node = state.node ?: return
        val nameValid = state.name.isNotBlank()
        val depthValid = node.type != SensorType.SONDA_SUELO || state.depthCm in setOf(30, 60)
        if (!nameValid || !depthValid) {
            _uiState.value = state.copy(
                nameError = !nameValid,
                depthError = !depthValid,
            )
            return
        }

        _uiState.value = state.copy(isSaving = true, error = null)
        viewModelScope.launch {
            val result = updateNode(
                node.copy(
                    name = state.name.trim(),
                    depthCm = if (node.type == SensorType.SONDA_SUELO) state.depthCm else node.depthCm,
                    status = if (state.transmitReadings) SensorStatus.ACTIVE else SensorStatus.PAUSED,
                ),
            )
            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, node = result.value)
                    onSuccess()
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, error = result.error)
                }
            }
        }
    }

    fun unlink(onSuccess: () -> Unit) {
        if (_uiState.value.isUnlinking) return
        _uiState.value = _uiState.value.copy(isUnlinking = true, error = null)
        viewModelScope.launch {
            when (val result = unlinkNode(plotId, nodeId)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isUnlinking = false)
                    onSuccess()
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isUnlinking = false, error = result.error)
                }
            }
        }
    }
}
