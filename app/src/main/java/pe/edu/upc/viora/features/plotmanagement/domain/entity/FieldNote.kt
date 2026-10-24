package pe.edu.upc.viora.features.plotmanagement.domain.entity

import java.time.Instant
import java.time.LocalDate

/** What a logbook note is about: the chips of P75. */
enum class FieldNoteType { IRRIGATION, PRUNING, FERTILIZATION, PESTS, OTHER }

/** Where a note is: only on the phone, on the server, or rejected by the server (it stays on the phone). */
enum class FieldNoteSyncState { PENDING, SYNCED, FAILED }

/**
 * A note the producer wrote in the logbook about one plot (P75). It is saved on the phone first and
 * sent when there is signal; [hasPhoto] is true when a photo was attached (still on the phone while
 * [syncState] is [FieldNoteSyncState.PENDING]).
 */
data class FieldNote(
    val id: String,
    val plotId: String,
    val type: FieldNoteType,
    val text: String,
    val observedOn: LocalDate,
    val createdAt: Instant,
    val hasPhoto: Boolean,
    val syncState: FieldNoteSyncState,
) {
    companion object {
        /** The server keeps up to 500 characters. */
        const val MAX_TEXT_LENGTH = 500
    }
}

/** A note being written: the photo is the path of a JPEG already reduced to fit the server limit. */
data class NewFieldNote(
    val plotId: String,
    val type: FieldNoteType,
    val text: String,
    val observedOn: LocalDate,
    val photoPath: String?,
)

/** Result of a sync pass: everything sent (or rejected for good), or something to retry later. */
enum class FieldNoteSyncResult { DONE, RETRY_LATER }
