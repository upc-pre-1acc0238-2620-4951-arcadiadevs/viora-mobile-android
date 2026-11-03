package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewFieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.repository.FieldNoteRepository

/** The logbook notes on the phone, newest first. */
class ObserveFieldNotesUseCase @Inject constructor(private val repository: FieldNoteRepository) {
    operator fun invoke(): Flow<List<FieldNote>> = repository.observeNotes()
}

/** Writes a note (P75): blank text or text over the server limit is refused before saving. */
class WriteFieldNoteUseCase @Inject constructor(private val repository: FieldNoteRepository) {
    suspend operator fun invoke(note: NewFieldNote): AppResult<FieldNote> {
        val text = note.text.trim()
        if (text.isEmpty() || text.length > FieldNote.MAX_TEXT_LENGTH) {
            return AppResult.Failure(AppError.Validation(code = INVALID_TEXT))
        }
        return repository.write(note.copy(text = text))
    }

    companion object {
        const val INVALID_TEXT = "orchard.field_note.text.invalid"
    }
}

/** The photo of a note already on the server (P79), as a path on the phone. */
class GetFieldNotePhotoUseCase @Inject constructor(private val repository: FieldNoteRepository) {
    suspend operator fun invoke(plotId: String, noteId: String): AppResult<String> = repository.photoPath(plotId, noteId)
}

/** Brings the notes of the given plots up to date with the server. */
class RefreshFieldNotesUseCase @Inject constructor(private val repository: FieldNoteRepository) {
    suspend operator fun invoke(plotIds: List<String>): AppResult<Unit> = repository.refresh(plotIds)
}
