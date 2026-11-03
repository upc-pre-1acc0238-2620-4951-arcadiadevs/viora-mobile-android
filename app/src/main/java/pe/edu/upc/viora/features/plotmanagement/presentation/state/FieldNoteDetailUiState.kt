package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote

/** The photo of a note in P79: none, on its way, ready at a path on the phone, or out of reach for now. */
sealed interface NotePhotoUiState {
    data object None : NotePhotoUiState
    data object Loading : NotePhotoUiState
    data class Ready(val path: String) : NotePhotoUiState
    data object Unavailable : NotePhotoUiState
}

/** P79 · Detalle de nota. [note] is null until the note is read from the phone; [plotName] null when the plot is gone. */
data class FieldNoteDetailUiState(
    val note: FieldNote? = null,
    val plotName: String? = null,
    val photo: NotePhotoUiState = NotePhotoUiState.None,
)
