package pe.edu.upc.viora.features.harvestsettlement.infrastructure.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import pe.edu.upc.viora.features.harvestsettlement.domain.service.SettlementSyncScheduler

/**
 * Runs [SettlementSyncWorker] once the network is connected, retrying with exponential backoff.
 * The work is unique and appended, so a request made while a pass is running still gets its own
 * pass afterwards instead of being dropped.
 */
class WorkManagerSettlementSyncScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SettlementSyncScheduler {

    override fun schedule() {
        val request = OneTimeWorkRequestBuilder<SettlementSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "settlement-sync"
        const val BACKOFF_SECONDS = 30L
    }
}
