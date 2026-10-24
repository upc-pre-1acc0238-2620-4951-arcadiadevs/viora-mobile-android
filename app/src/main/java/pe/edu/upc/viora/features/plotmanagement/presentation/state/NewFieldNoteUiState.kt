package pe.edu.upc.viora.features.plotmanagement.presentation.state

import java.time.LocalDate
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteType
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

/** P75 · Nueva nota: plot and type chips, the text and an optional photo. The note is dated today. */
data class NewFieldNoteUiState(
    val plots: List<Plot> = emptyList(),
    val plotId: String? = null,
    val type: FieldNoteType = FieldNoteType.IRRIGATION,
    val text: String = "",
    val photoPath: String? = null,
    val isPreparingPhoto: Boolean = false,
    val photoFailed: Boolean = false,
    val isSaving: Boolean = false,
    val today: LocalDate = LocalDate.now(),
) {
    val canSave: Boolean
        get() = plotId != null && text.isNotBlank() && text.trim().length <= FieldNote.MAX_TEXT_LENGTH &&
            !isSaving && !isPreparingPhoto
}
