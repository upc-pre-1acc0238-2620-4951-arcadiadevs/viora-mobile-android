package pe.edu.upc.viora.features.croploadregulation.infrastructure.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SyncDraftSamplesUseCase

/** Thin WorkManager shell: the rules live in [SyncDraftSamplesUseCase]. */
@HiltWorker
class SamplingSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncDraftSamples: SyncDraftSamplesUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        if (syncDraftSamples.syncAllPending().pendingLeft) Result.retry() else Result.success()
}
