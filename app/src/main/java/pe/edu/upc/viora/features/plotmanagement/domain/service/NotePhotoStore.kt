package pe.edu.upc.viora.features.plotmanagement.domain.service

/** Keeps the photo of a note on the phone, reduced to what the server accepts, until it is uploaded. */
interface NotePhotoStore {

    /**
     * Copies the picked image as a JPEG of at most 1 MiB.
     *
     * @param uri content URI from the photo picker
     * @return path of the stored JPEG, or null when the image cannot be read
     */
    suspend fun keep(uri: String): String?

    /** Deletes a photo kept for a note that was discarded before saving. */
    suspend fun discard(path: String)

    /** Path of the photo of [noteId] downloaded earlier, or null when it has to be downloaded. */
    suspend fun downloaded(noteId: String): String?

    /** Keeps a downloaded photo in the cache; the system may delete it, it can be downloaded again. */
    suspend fun keepDownloaded(noteId: String, bytes: ByteArray): String
}

/** Folder under the cache where the photos downloaded from the server stay. */
const val DOWNLOADED_NOTE_PHOTOS_DIR = "field-note-photos"

/** The photo of a note always has the same file name; the id comes from the server, so only a safe one is accepted. */
fun downloadedNotePhotoName(noteId: String): String {
    require(noteId.isNotEmpty() && noteId.all { it.isLetterOrDigit() || it == '-' }) { "Unexpected note id" }
    return "$noteId.jpg"
}
