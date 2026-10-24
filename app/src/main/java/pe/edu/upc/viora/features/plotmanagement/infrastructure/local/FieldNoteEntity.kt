package pe.edu.upc.viora.features.plotmanagement.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * A logbook note on the phone. [syncState] is the domain `FieldNoteSyncState` name; [photoPath] is the
 * local JPEG while it has not been uploaded ([photoPending]), and is cleared once it is on the server.
 * [observedOn] is an ISO date and [createdAt] epoch milliseconds.
 */
@Entity(tableName = "field_notes", indices = [Index(value = ["plot_id"])])
data class FieldNoteEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    val type: String,
    val text: String,
    @ColumnInfo(name = "observed_on")
    val observedOn: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "has_photo")
    val hasPhoto: Boolean,
    @ColumnInfo(name = "photo_path")
    val photoPath: String?,
    @ColumnInfo(name = "photo_pending")
    val photoPending: Boolean,
    @ColumnInfo(name = "sync_state")
    val syncState: String,
    @ColumnInfo(name = "last_error_code")
    val lastErrorCode: String?,
)
