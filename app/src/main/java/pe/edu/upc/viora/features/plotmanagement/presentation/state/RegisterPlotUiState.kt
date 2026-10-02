package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline

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
    val corners: List<GeoPoint> = emptyList(),
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
    val isSaved: Boolean = false,
) {
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
