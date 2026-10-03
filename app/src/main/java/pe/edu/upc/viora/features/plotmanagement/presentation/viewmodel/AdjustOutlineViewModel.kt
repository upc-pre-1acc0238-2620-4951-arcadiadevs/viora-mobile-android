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
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.state.AdjustOutlineUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.state.TracedCorner

@HiltViewModel
class AdjustOutlineViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlot: ObservePlotUseCase,
    private val updatePlot: UpdatePlotUseCase,
) : ViewModel() {

    // Read by name, as in PlotDetailViewModel, so the view model can be tested on the JVM.
    private val plotId = PlotId(checkNotNull(savedStateHandle.get<String>(KEY_PLOT_ID)) { "AdjustOutlineRoute needs a plotId" })

    private val _uiState = MutableStateFlow<AdjustOutlineUiState>(AdjustOutlineUiState.Loading)
    val uiState: StateFlow<AdjustOutlineUiState> = _uiState.asStateFlow()

    init {
        // Starts from the cached plot; the outline is not refreshed while it is being adjusted.
        viewModelScope.launch {
            val plot = observePlot(plotId).first()
            _uiState.value = if (plot == null) {
                AdjustOutlineUiState.NotFound
            } else {
                val corners = plot.outline.mapIndexed { index, point -> TracedCorner(id = index, point = point) }
                AdjustOutlineUiState.Adjusting(plot = plot, corners = corners, original = corners)
            }
        }
    }

    /** Moves a corner. One drag is one undo step: the outline is remembered when a different corner starts moving. */
    fun moveCorner(id: Int, point: GeoPoint) = adjust { state ->
        val index = state.corners.indexOfFirst { it.id == id }
        if (index < 0) return@adjust state
        val moved = state.corners.toMutableList().apply { this[index] = this[index].copy(point = point) }
        if (PlotOutline.check(moved.map { it.point }) == PlotOutline.Error.SelfIntersecting) {
            state.copy(refusedMove = true)
        } else {
            state.copy(
                corners = moved,
                history = if (state.lastMovedId == id) state.history else state.history + listOf(state.corners),
                lastMovedId = id,
                refusedMove = false,
                outlineError = null,
                failure = null,
            )
        }
    }

    /** Adds a corner where the crosshair is, between the two corners that keep the edges from crossing. */
    fun addCorner(point: GeoPoint) = adjust { state ->
        val index = PlotOutline.insertionIndex(state.points, point)
            ?: return@adjust state.copy(outlineError = PlotOutline.Error.SelfIntersecting)
        state.copy(
            corners = state.corners.toMutableList().apply { add(index, TracedCorner(id = state.nextCornerId, point = point)) },
            history = state.history + listOf(state.corners),
            nextCornerId = state.nextCornerId + 1,
            lastMovedId = null,
            outlineError = null,
            refusedMove = false,
            failure = null,
        )
    }

    fun undo() = adjust { state ->
        val previous = state.history.lastOrNull() ?: return@adjust state
        state.copy(
            corners = previous,
            history = state.history.dropLast(1),
            lastMovedId = null,
            outlineError = null,
            refusedMove = false,
            failure = null,
        )
    }

    fun save() {
        val current = _uiState.value as? AdjustOutlineUiState.Adjusting ?: return
        if (!current.canSave) return
        val plot = current.plot
        // The name and frame come back as the plot already has them; only the outline changes.
        val changes = runCatching {
            PlotChanges(
                name = PlotName.of(plot.name),
                variety = plot.variety,
                frame = PlantationFrame(plot.rowSpacingMeters, plot.treeSpacingMeters),
                outline = PlotOutline(current.points),
            )
        }.getOrElse {
            adjust { it.copy(failure = EditFailure.Rejected(null)) }
            return
        }
        adjust { it.copy(isSaving = true, failure = null) }
        viewModelScope.launch {
            when (val result = updatePlot(plotId, changes)) {
                is AppResult.Success -> adjust { it.copy(isSaving = false, isSaved = true) }
                is AppResult.Failure -> adjust { it.copy(isSaving = false, failure = result.error.toFailure()) }
            }
        }
    }

    private fun AppError.toFailure(): EditFailure = when (this) {
        is AppError.PreconditionFailed -> EditFailure.Outdated
        is AppError.Validation -> EditFailure.Rejected(detail)
        else -> EditFailure.Other(this)
    }

    private fun adjust(transform: (AdjustOutlineUiState.Adjusting) -> AdjustOutlineUiState.Adjusting) {
        _uiState.update { state -> if (state is AdjustOutlineUiState.Adjusting) transform(state) else state }
    }

    private companion object {
        const val KEY_PLOT_ID = "plotId"
    }
}
