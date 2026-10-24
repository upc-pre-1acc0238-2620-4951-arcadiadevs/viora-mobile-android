package pe.edu.upc.viora.features.plotmanagement.infrastructure.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncResult
import pe.edu.upc.viora.features.plotmanagement.domain.repository.FieldNoteRepository

/** Thin WorkManager shell: the rules live in [FieldNoteRepository.syncPending]. */
@HiltWorker
class FieldNoteSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: FieldNoteRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (repository.syncPending()) {
        FieldNoteSyncResult.DONE -> Result.success()
        FieldNoteSyncResult.RETRY_LATER -> Result.retry()
    }
}
