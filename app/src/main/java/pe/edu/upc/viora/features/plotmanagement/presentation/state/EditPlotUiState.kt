package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import kotlin.math.roundToInt

/** Why saving a plot's edits failed. */
sealed interface EditFailure {
    data object NameTaken : EditFailure

    /** The plot changed on another device after it was opened here. */
    data object Outdated : EditFailure

    data class Rejected(val detail: String?) : EditFailure

    data class Other(val error: AppError) : EditFailure
}

/** The edit form (Figma P28): name, variety and planting frame of a registered plot. */
sealed interface EditPlotUiState {

    data object Loading : EditPlotUiState

    data object NotFound : EditPlotUiState

    /**
     * The spacings are kept as the text typed in their fields ([rowSpacingText], [treeSpacingText])
     * so the producer can clear a field and type a new distance; [rowSpacingMeters] and
     * [treeSpacingMeters] are what that text means, or null when it is not a distance yet.
     */
    data class Editing(
        val original: Plot,
        val name: String,
        val variety: OliveVariety,
        val rowSpacingText: String,
        val treeSpacingText: String,
        val isChoosingVariety: Boolean = false,
        val isSaving: Boolean = false,
        val failure: EditFailure? = null,
        val isSaved: Boolean = false,
    ) : EditPlotUiState {
        val rowSpacingMeters: Double? get() = rowSpacingText.toMeters()
        val treeSpacingMeters: Double? get() = treeSpacingText.toMeters()

        val nameError: PlotName.Error? get() = PlotName.check(name)

        /** Null when both distances make a viable frame. */
        val frameError: PlantationFrame.Error?
            get() {
                val row = rowSpacingMeters
                val tree = treeSpacingMeters
                return if (row == null || tree == null) PlantationFrame.Error.NOT_POSITIVE else PlantationFrame.check(row, tree)
            }

        val isValid: Boolean get() = nameError == null && frameError == null

        /** Trees per hectare of the frame being typed, or null while it is not viable. */
        val treesPerHectare: Int?
            get() {
                val row = rowSpacingMeters
                val tree = treeSpacingMeters
                return if (frameError == null && row != null && tree != null) PlantationFrame.density(row, tree) else null
            }

        /** Trees in the whole plot with the frame being typed, or null while it is not viable. */
        val estimatedTrees: Int? get() = treesPerHectare?.let { (original.areaHectares * it).roundToInt() }

        /** The density the frame would have even if it is not viable, to show how far off it is. */
        val rawDensity: Int?
            get() {
                val row = rowSpacingMeters
                val tree = treeSpacingMeters
                return if (row != null && tree != null) PlantationFrame.density(row, tree) else null
            }

        val hasChanges: Boolean
            get() = name.trim() != original.name ||
                variety != original.variety ||
                rowSpacingMeters != original.rowSpacingMeters ||
                treeSpacingMeters != original.treeSpacingMeters
    }
}

/** A distance typed in meters: a positive number, with a comma or a dot as decimal separator. */
internal fun String.toMeters(): Double? = trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
