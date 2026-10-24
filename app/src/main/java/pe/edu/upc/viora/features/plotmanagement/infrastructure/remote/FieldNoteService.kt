package pe.edu.upc.viora.features.plotmanagement.infrastructure.remote

import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** `FieldNoteResource` of the backend (`/api/v1/plots/{plotId}/field-notes`). */
@Serializable
data class FieldNoteDto(
    val id: String,
    val plotId: String,
    val type: String,
    val text: String,
    val observedOn: String,
    val recordedAt: String,
    val hasPhoto: Boolean,
)

@Serializable
data class RecordFieldNoteRequestDto(
    val id: String,
    val type: String,
    val text: String,
    val observedOn: String,
)

interface FieldNoteService {

    /** Notes of a plot, newest first. */
    @GET("plots/{plotId}/field-notes")
    suspend fun getNotes(@Path("plotId") plotId: String): Response<List<FieldNoteDto>>

    /**
     * Stores a note. 201 when created, 200 when the same note (same id and content) was already
     * stored; 400 invalid; 404 the plot is not active; 409 the id belongs to another note.
     */
    @POST("plots/{plotId}/field-notes")
    suspend fun record(@Path("plotId") plotId: String, @Body request: RecordFieldNoteRequestDto): Response<FieldNoteDto>

    /** Attaches or replaces the photo (raw JPEG body, up to 1 MiB). */
    @PUT("plots/{plotId}/field-notes/{noteId}/photo")
    suspend fun attachPhoto(
        @Path("plotId") plotId: String,
        @Path("noteId") noteId: String,
        @Body photo: RequestBody,
    ): Response<FieldNoteDto>
}
