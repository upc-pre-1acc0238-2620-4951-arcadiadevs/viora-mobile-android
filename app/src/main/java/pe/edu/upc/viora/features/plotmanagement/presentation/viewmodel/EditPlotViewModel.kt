package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.UpdatePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditPlotUiState

@HiltViewModel
class EditPlotViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlot: ObservePlotUseCase,
    private val updatePlot: UpdatePlotUseCase,
) : ViewModel() {

    // Read by name, as in PlotDetailViewModel, so the view model can be tested on the JVM.
    private val plotId = PlotId(checkNotNull(savedStateHandle.get<String>(KEY_PLOT_ID)) { "EditPlotRoute needs a plotId" })

    private val _uiState = MutableStateFlow<EditPlotUiState>(EditPlotUiState.Loading)
    val uiState: StateFlow<EditPlotUiState> = _uiState.asStateFlow()

    init {
        // The form starts from the cached plot and is not refreshed while it is being edited.
        viewModelScope.launch {
            val plot = observePlot(plotId).first()
            _uiState.value = if (plot == null) {
                EditPlotUiState.NotFound
            } else {
                EditPlotUiState.Editing(
                    original = plot,
                    name = plot.name,
                    variety = plot.variety,
                    rowSpacingText = plot.rowSpacingMeters.asText(),
                    treeSpacingText = plot.treeSpacingMeters.asText(),
                )
            }
        }
    }

    fun setName(value: String) = edit { it.copy(name = value, failure = null) }

    fun setVariety(value: OliveVariety) = edit { it.copy(variety = value, isChoosingVariety = false) }

    fun setRowSpacing(text: String) = edit { it.copy(rowSpacingText = text.asTypedDistance(), failure = null) }

    fun setTreeSpacing(text: String) = edit { it.copy(treeSpacingText = text.asTypedDistance(), failure = null) }

    fun toggleVarietyChoice() = edit { it.copy(isChoosingVariety = !it.isChoosingVariety) }

    fun save() {
        val current = _uiState.value as? EditPlotUiState.Editing ?: return
        if (current.isSaving) return
        if (!current.isValid) return
        val changes = PlotChanges(
            name = PlotName.of(current.name),
            variety = current.variety,
            frame = PlantationFrame(checkNotNull(current.rowSpacingMeters), checkNotNull(current.treeSpacingMeters)),
        )
        edit { it.copy(isSaving = true, failure = null) }
        viewModelScope.launch {
            when (val result = updatePlot(plotId, changes)) {
                is AppResult.Success -> edit { it.copy(isSaving = false, isSaved = true) }
                is AppResult.Failure -> edit { it.copy(isSaving = false, failure = result.error.toFailure()) }
            }
        }
    }

    private fun AppError.toFailure(): EditFailure = when (this) {
        is AppError.Conflict -> EditFailure.NameTaken
        is AppError.PreconditionFailed -> EditFailure.Outdated
        is AppError.Validation -> EditFailure.Rejected(detail)
        else -> EditFailure.Other(this)
    }

    private fun edit(transform: (EditPlotUiState.Editing) -> EditPlotUiState.Editing) {
        _uiState.update { state -> if (state is EditPlotUiState.Editing) transform(state) else state }
    }

    /** 10.0 is shown as "10" and 7.5 as "7.5", as a producer would write them. */
    private fun Double.asText(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()

    /** Keeps only what can be part of a distance: digits and one decimal separator, at most five characters. */
    private fun String.asTypedDistance(): String = filter { it.isDigit() || it == '.' || it == ',' }.take(MAX_DISTANCE_LENGTH)

    private companion object {
        const val KEY_PLOT_ID = "plotId"
        const val MAX_DISTANCE_LENGTH = 5
    }
}
