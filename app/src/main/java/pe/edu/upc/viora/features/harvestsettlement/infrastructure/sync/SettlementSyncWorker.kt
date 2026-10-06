package pe.edu.upc.viora.features.harvestsettlement.infrastructure.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSyncResult
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository

/**
 * Thin WorkManager shell: the sync rules live in [HarvestSettlementRepository.syncPending] (where
 * they are unit-tested); the worker only maps its result to WorkManager's retry/backoff.
 */
@HiltWorker
class SettlementSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: HarvestSettlementRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (repository.syncPending()) {
        SettlementSyncResult.DONE -> Result.success()
        SettlementSyncResult.RETRY_LATER -> Result.retry()
    }
}
