package pe.edu.upc.viora.features.plotmanagement.infrastructure.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.plotmanagement.domain.service.DOWNLOADED_NOTE_PHOTOS_DIR
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.FieldNoteRepositoryImpl

/** Photos of field notes, waiting to be sent or downloaded to be seen, belong to the account that owns the notes. */
class FieldNotePhotosWiper @Inject constructor(@ApplicationContext private val context: Context) : AccountDataWiper {
    override suspend fun wipe() {
        File(context.filesDir, FieldNoteRepositoryImpl.PHOTO_DIR).deleteRecursively()
        File(context.cacheDir, DOWNLOADED_NOTE_PHOTOS_DIR).deleteRecursively()
    }
}
