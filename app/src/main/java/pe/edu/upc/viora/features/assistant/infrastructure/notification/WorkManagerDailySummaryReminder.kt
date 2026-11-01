package pe.edu.upc.viora.features.assistant.infrastructure.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import pe.edu.upc.viora.features.assistant.domain.service.DailySummaryReminder
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Runs [DailySummaryWorker] once a day around 07:00 on the phone, after the server wrote the summaries at 06:30
 * Lima. WorkManager may delay it a little to save battery; it needs the network to read the summary. Signing out
 * cancels every work, so the Inicio of the next session schedules it again.
 */
class WorkManagerDailySummaryReminder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: Clock,
    private val zone: ZoneId,
) : DailySummaryReminder {

    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<DailySummaryWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(untilNextMorning().toMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** How long until the next 07:00 on the phone: today's if it has not passed, otherwise tomorrow's. */
    internal fun untilNextMorning(): Duration {
        val now = ZonedDateTime.now(clock.withZone(zone))
        var next = now.with(MORNING)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "assistant-daily-summary"
        val MORNING: LocalTime = LocalTime.of(7, 0)
    }
}
