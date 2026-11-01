package pe.edu.upc.viora.features.assistant.infrastructure.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.notification.VioraNotificationChannels
import pe.edu.upc.viora.features.assistant.domain.entity.DailySummary
import javax.inject.Inject

/**
 * Shows the summary of the day as a local notification; tapping it opens the app on Inicio. Nothing is shown
 * when the producer did not allow notifications (onboarding asks for it) or turned the channel off.
 */
class DailySummaryNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun show(summary: DailySummary) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        VioraNotificationChannels.ensureDailySummary(context)
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val tap = open?.let {
            PendingIntent.getActivity(context, REQUEST_CODE, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(context, VioraNotificationChannels.DAILY_SUMMARY)
            .setSmallIcon(R.drawable.ic_isotype)
            .setContentTitle(context.getString(R.string.notification_daily_summary_title))
            .setContentText(summary.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary.text))
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        /** One summary a day: the new one replaces yesterday's if it is still there. */
        const val NOTIFICATION_ID = 7001
        const val REQUEST_CODE = 7001
    }
}
