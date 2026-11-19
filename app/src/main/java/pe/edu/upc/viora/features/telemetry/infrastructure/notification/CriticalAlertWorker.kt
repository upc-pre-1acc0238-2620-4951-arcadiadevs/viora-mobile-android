package pe.edu.upc.viora.features.telemetry.infrastructure.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.features.telemetry.application.usecase.CheckCriticalAlertsUseCase

@HiltWorker
class CriticalAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val check: CheckCriticalAlertsUseCase,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = when (val result = check()) {
        is AppResult.Success -> Result.success()
        is AppResult.Failure -> if (result.error.isConnectivity && runAttemptCount < 4) Result.retry() else Result.success()
    }
}
