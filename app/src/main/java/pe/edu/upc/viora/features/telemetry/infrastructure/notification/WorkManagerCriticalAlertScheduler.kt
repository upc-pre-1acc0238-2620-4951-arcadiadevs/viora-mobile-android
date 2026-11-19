package pe.edu.upc.viora.features.telemetry.infrastructure.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertScheduler

class WorkManagerCriticalAlertScheduler @Inject constructor(@param:ApplicationContext private val context: Context) : CriticalAlertScheduler {
    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<CriticalAlertWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object { const val WORK_NAME = "telemetry-critical-alerts" }
}
