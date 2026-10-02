package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

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
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.RegisterPlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.SaveFailure

/** Drives the three-step wizard: trace the outline, describe the plot, review and save. */
@HiltViewModel
class RegisterPlotViewModel @Inject constructor(
    observePlots: ObservePlotsUseCase,
    private val registerPlot: RegisterPlotUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterPlotUiState())
    val uiState: StateFlow<RegisterPlotUiState> = _uiState.asStateFlow()

    init {
        // Open the map where the producer already has plots (they are cached, so this is instant).
        viewModelScope.launch {
            val center = observePlots().first().firstNotNullOfOrNull { centerOf(it.outline) }
            _uiState.update { it.copy(mapCenter = center) }
        }
    }

    // ---- Step 1: trace

    fun addCorner(point: GeoPoint) {
        _uiState.update {
            if (it.step != RegisterPlotStep.TRACE) it else it.copy(corners = it.corners + point, outlineError = null)
        }
    }

    fun undoCorner() {
        _uiState.update { it.copy(corners = it.corners.dropLast(1), outlineError = null) }
    }

    /** Validates the traced outline and moves on, or explains what is wrong with it. */
    fun closeOutline() {
        _uiState.update {
            val error = PlotOutline.check(it.corners)
            if (error == null) it.copy(step = RegisterPlotStep.DETAILS, outlineError = null) else it.copy(outlineError = error)
        }
    }

    // ---- Step 2: details

    fun setName(value: String) {
        _uiState.update { it.copy(name = value, saveFailure = null) }
    }

    fun setVariety(value: OliveVariety) {
        _uiState.update { it.copy(variety = value) }
    }

    fun changeRowSpacing(steps: Int) {
        _uiState.update { it.copy(rowSpacingMeters = it.rowSpacingMeters.stepped(steps)) }
    }

    fun changeTreeSpacing(steps: Int) {
        _uiState.update { it.copy(treeSpacingMeters = it.treeSpacingMeters.stepped(steps)) }
    }

    /** Moves to the review when the details are valid; otherwise starts showing the hints. */
    fun continueToReview() {
        _uiState.update {
            if (it.detailsAreValid) it.copy(step = RegisterPlotStep.REVIEW) else it.copy(showDetailErrors = true)
        }
    }

    // ---- Step 3: review and save

    fun save() {
        val current = _uiState.value
        if (current.isSaving) return
        val newPlot = current.toNewPlotOrNull() ?: return
        _uiState.update { it.copy(isSaving = true, saveFailure = null) }
        viewModelScope.launch {
            when (val result = registerPlot(newPlot)) {
                is AppResult.Success -> _uiState.update { it.copy(isSaving = false, isSaved = true) }
                is AppResult.Failure -> _uiState.update { it.afterSaveFailure(result.error) }
            }
        }
    }

    fun dismissSaveFailure() {
        _uiState.update { it.copy(saveFailure = null) }
    }

    // ---- Navigation inside the wizard

    /**
     * Goes back one step. Returns `false` when already on the first step, meaning the caller
     * should leave the wizard (after confirming if there is work in progress).
     */
    fun goBack(): Boolean {
        val previous = when (_uiState.value.step) {
            RegisterPlotStep.TRACE -> return false
            RegisterPlotStep.DETAILS -> RegisterPlotStep.TRACE
            RegisterPlotStep.REVIEW -> RegisterPlotStep.DETAILS
        }
        _uiState.update { it.copy(step = previous, saveFailure = null) }
        return true
    }

    private fun RegisterPlotUiState.afterSaveFailure(error: AppError): RegisterPlotUiState = when (error) {
        // A taken name is fixed in the details step, so take the producer there.
        is AppError.Conflict -> copy(isSaving = false, saveFailure = SaveFailure.NameTaken, step = RegisterPlotStep.DETAILS)
        is AppError.Validation -> copy(isSaving = false, saveFailure = SaveFailure.Rejected(error.detail))
        else -> copy(isSaving = false, saveFailure = SaveFailure.Other(error))
    }

    private fun RegisterPlotUiState.toNewPlotOrNull(): NewPlot? {
        val variety = variety ?: return null
        if (!detailsAreValid || PlotOutline.check(corners) != null) return null
        return NewPlot(
            name = PlotName.of(name),
            variety = variety,
            outline = PlotOutline(corners),
            frame = PlantationFrame(rowSpacingMeters, treeSpacingMeters),
        )
    }

    private fun Double.stepped(steps: Int): Double =
        (this + steps * RegisterPlotUiState.SPACING_STEP_METERS)
            .coerceIn(RegisterPlotUiState.MIN_SPACING_METERS, RegisterPlotUiState.MAX_SPACING_METERS)

    private fun centerOf(outline: List<GeoPoint>): GeoPoint? =
        if (outline.isEmpty()) null else GeoPoint(outline.map { it.latitude }.average(), outline.map { it.longitude }.average())
}
