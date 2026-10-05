package pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.fold
import pe.edu.upc.viora.features.croploadregulation.application.usecase.GetPlotSamplingOverviewUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus
import pe.edu.upc.viora.features.croploadregulation.presentation.state.SelectPlotSamplingUiState

@HiltViewModel
class SelectPlotSamplingViewModel @Inject constructor(
    private val getPlotSamplingOverview: GetPlotSamplingOverviewUseCase,
) : ViewModel() {

    private val currentYear = java.time.Year.now().value
    private val _uiState = MutableStateFlow<SelectPlotSamplingUiState>(SelectPlotSamplingUiState.Loading)
    val uiState: StateFlow<SelectPlotSamplingUiState> = _uiState.asStateFlow()

    init {
        loadPlots()
    }

    fun selectPlot(plotId: String) {
        _uiState.update { current ->
            if (current is SelectPlotSamplingUiState.Content) {
                current.copy(selectedPlotId = plotId)
            } else {
                current
            }
        }
    }

    fun refresh() {
        val currentContent = _uiState.value as? SelectPlotSamplingUiState.Content
        if (currentContent != null) {
            _uiState.value = currentContent.copy(isRefreshing = true, error = null)
        } else {
            _uiState.value = SelectPlotSamplingUiState.Loading
        }
        loadPlots()
    }

    private fun loadPlots() {
        viewModelScope.launch {
            getPlotSamplingOverview(campaignYear = currentYear).fold(

                onSuccess = { plots ->
                    val defaultSelected = (_uiState.value as? SelectPlotSamplingUiState.Content)?.selectedPlotId
                        ?: plots.firstOrNull { it.samplingStatus == SamplingStatus.IN_PROGRESS }?.plotId
                        ?: plots.firstOrNull { it.samplingStatus == SamplingStatus.NOT_STARTED }?.plotId
                        ?: plots.firstOrNull()?.plotId

                    val campaign = plots.firstOrNull()?.campaignYear ?: currentYear

                    _uiState.value = SelectPlotSamplingUiState.Content(
                        plots = plots,
                        selectedPlotId = defaultSelected,
                        campaignYear = campaign,
                        isRefreshing = false,
                        error = null,
                    )
                },
                onFailure = { error ->
                    val currentContent = _uiState.value as? SelectPlotSamplingUiState.Content
                    _uiState.value = if (currentContent != null) {
                        currentContent.copy(isRefreshing = false, error = error)
                    } else {
                        SelectPlotSamplingUiState.Content(
                            plots = emptyList(),
                            selectedPlotId = null,
                            campaignYear = currentYear,
                            isRefreshing = false,
                            error = error,
                        )
                    }
                },
            )
        }
    }
}
