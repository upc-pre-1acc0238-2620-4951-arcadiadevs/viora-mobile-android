package pe.edu.upc.viora.features.croploadregulation.infrastructure.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SampleSyncOutcome
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SyncDraftSamplesUseCase

/** Thin WorkManager shell: the rules live in [SyncDraftSamplesUseCase]. */
@HiltWorker
class SamplingSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncDraftSamples: SyncDraftSamplesUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        if (shouldRetry(syncDraftSamples.syncAllPending(), runAttemptCount)) Result.retry() else Result.success()

    companion object {
        const val MAX_ATTEMPTS = 8

        /**
         * Losing the connection retries for as long as it takes. Any other failure retries up to [MAX_ATTEMPTS]
         * times and then stops: the drafts stay on the phone for the next send from the screen, like a settlement
         * that keeps failing for a reason the network does not explain.
         */
        fun shouldRetry(outcome: SampleSyncOutcome, runAttemptCount: Int): Boolean =
            outcome.pendingLeft && (outcome.offline || runAttemptCount + 1 < MAX_ATTEMPTS)
    }
}
