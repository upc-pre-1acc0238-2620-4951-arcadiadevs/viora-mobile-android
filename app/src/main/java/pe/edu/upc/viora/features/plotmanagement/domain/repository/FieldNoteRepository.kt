package pe.edu.upc.viora.features.plotmanagement.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewFieldNote

/** The logbook notes: written on the phone first, sent to the server when there is signal. */
interface FieldNoteRepository {

    /** Every note on the phone (sent or not), newest first. */
    fun observeNotes(): Flow<List<FieldNote>>

    /** Saves the note on the phone and asks for it to be sent; never fails for lack of signal. */
    suspend fun write(note: NewFieldNote): AppResult<FieldNote>

    /** Downloads the notes of the given plots, keeping the ones not sent yet. */
    suspend fun refresh(plotIds: List<String>): AppResult<Unit>

    /** Sends the notes not sent yet (and their photos). Called by the background worker. */
    suspend fun syncPending(): FieldNoteSyncResult
}
