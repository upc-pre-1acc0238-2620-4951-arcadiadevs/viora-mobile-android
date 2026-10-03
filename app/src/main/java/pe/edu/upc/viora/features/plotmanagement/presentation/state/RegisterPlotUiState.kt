package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline

/** A corner of the outline being traced; the [id] follows it even when the outline is reordered. */
data class TracedCorner(val id: Int, val point: GeoPoint)

/** The three steps of the registration wizard, in order. */
enum class RegisterPlotStep { TRACE, DETAILS, REVIEW }

/** Why saving did not work, in terms the screen can explain. */
sealed interface SaveFailure {
    /** The producer already has a plot with this name. */
    data object NameTaken : SaveFailure

    /** The server rejected the data (e.g. an invalid polygon); [detail] is its own explanation. */
    data class Rejected(val detail: String?) : SaveFailure

    /** Connection, timeout or server problems. */
    data class Other(val error: AppError) : SaveFailure
}

data class RegisterPlotUiState(
    val step: RegisterPlotStep = RegisterPlotStep.TRACE,
    /** The traced corners in drawing order (the outline never crosses itself). */
    val outline: List<TracedCorner> = emptyList(),
    /** Next id to hand out: ids grow in the order corners are added, so the highest is the last one. */
    val nextCornerId: Int = 0,
    /** The last attempt to drag a corner would have crossed two edges, so the corner was not moved. */
    val refusedMove: Boolean = false,
    /** Where to centre the map: the area of the producer's existing plots, if any. */
    val mapCenter: GeoPoint? = null,
    val outlineError: PlotOutline.Error? = null,
    val name: String = "",
    val variety: OliveVariety? = null,
    val rowSpacingMeters: Double = DEFAULT_SPACING_METERS,
    val treeSpacingMeters: Double = DEFAULT_SPACING_METERS,
    /** Becomes true after a first failed attempt to continue, so hints do not nag while typing. */
    val showDetailErrors: Boolean = false,
    val isSaving: Boolean = false,
    val saveFailure: SaveFailure? = null,
    /** The id of the plot once it has been saved, so the screen can open its detail. */
    val savedPlotId: PlotId? = null,
) {
    val isSaved: Boolean get() = savedPlotId != null

    val corners: List<GeoPoint> get() = outline.map { it.point }

    val areaHectares: Double get() = PlotOutline.areaHectares(corners)
    val treesPerHectare: Int get() = PlantationFrame.density(rowSpacingMeters, treeSpacingMeters)
    val estimatedTrees: Int get() = Math.round(areaHectares * treesPerHectare).toInt()

    val nameError: PlotName.Error? get() = PlotName.check(name)
    val frameError: PlantationFrame.Error? get() = PlantationFrame.check(rowSpacingMeters, treeSpacingMeters)
    val detailsAreValid: Boolean get() = nameError == null && variety != null && frameError == null

    /** Leaving now would throw away something the producer did. */
    val hasWorkInProgress: Boolean get() = corners.isNotEmpty() || name.isNotBlank()

    companion object {
        const val DEFAULT_SPACING_METERS = 7.0
        const val MIN_SPACING_METERS = 1.0
        const val MAX_SPACING_METERS = 20.0
        const val SPACING_STEP_METERS = 0.5
    }
}
