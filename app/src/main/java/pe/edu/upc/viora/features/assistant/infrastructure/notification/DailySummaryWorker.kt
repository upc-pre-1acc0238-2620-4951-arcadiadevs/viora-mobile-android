package pe.edu.upc.viora.features.assistant.infrastructure.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.features.assistant.application.usecase.LoadDailySummaryUseCase

/**
 * The morning reminder of Inicio: reads the summary of the day (the server wrote it at 06:30) and shows it as a
 * notification. Without signal it tries again later; any other failure (no plan, no plots, signed out) waits for
 * the next morning, with no notification.
 */
@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val loadDailySummary: LoadDailySummaryUseCase,
    private val notifier: DailySummaryNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (val result = loadDailySummary()) {
        is AppResult.Success -> {
            notifier.show(result.value)
            Result.success()
        }
        is AppResult.Failure -> if (result.error.isConnectivity && runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.success()
    }

    private companion object {
        /** A morning without signal stops retrying before the day is over. */
        const val MAX_ATTEMPTS = 4
    }
}
