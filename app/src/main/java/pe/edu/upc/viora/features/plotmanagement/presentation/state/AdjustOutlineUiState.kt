package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline

/** The outline adjustment (Figma P29): the corners of a registered plot, moved on the map. */
sealed interface AdjustOutlineUiState {

    data object Loading : AdjustOutlineUiState

    data object NotFound : AdjustOutlineUiState

    /**
     * [corners] are the outline being adjusted and [original] the one the plot had when the screen
     * opened (both keep each corner's id, so a moved corner can be told from the others).
     * [history] holds the outlines before each change, newest last, for undo.
     */
    data class Adjusting(
        val plot: Plot,
        val corners: List<TracedCorner>,
        val original: List<TracedCorner>,
        val history: List<List<TracedCorner>> = emptyList(),
        val nextCornerId: Int = original.size,
        val lastMovedId: Int? = null,
        val outlineError: PlotOutline.Error? = null,
        val refusedMove: Boolean = false,
        val isSaving: Boolean = false,
        val failure: EditFailure? = null,
        val isSaved: Boolean = false,
    ) : AdjustOutlineUiState {
        val points: List<GeoPoint> get() = corners.map { it.point }

        val areaHectares: Double get() = PlotOutline.areaHectares(points)

        val outlineCenter: GeoPoint?
            get() = if (corners.isEmpty()) null else GeoPoint(points.map { it.latitude }.average(), points.map { it.longitude }.average())

        /** How many corners of the original outline are now somewhere else. */
        val movedCount: Int
            get() {
                val now = corners.associateBy { it.id }
                return original.count { corner -> now[corner.id]?.point?.let { it != corner.point } ?: false }
            }

        val canUndo: Boolean get() = history.isNotEmpty()

        val hasChanges: Boolean get() = corners != original

        val canSave: Boolean get() = hasChanges && PlotOutline.check(points) == null && !isSaving
    }
}
