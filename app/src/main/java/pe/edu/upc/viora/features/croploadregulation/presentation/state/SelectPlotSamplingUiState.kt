package pe.edu.upc.viora.features.croploadregulation.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview

sealed interface SelectPlotSamplingUiState {
    data object Loading : SelectPlotSamplingUiState

    data class Content(
        val plots: List<PlotSamplingOverview>,
        val selectedPlotId: String?,
        val campaignYear: Int = java.time.Year.now().value,
        val isRefreshing: Boolean = false,
        val error: AppError? = null,
    ) : SelectPlotSamplingUiState {
        val selectedPlot: PlotSamplingOverview?
            get() = plots.find { it.plotId == selectedPlotId }
    }
}
