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
}
