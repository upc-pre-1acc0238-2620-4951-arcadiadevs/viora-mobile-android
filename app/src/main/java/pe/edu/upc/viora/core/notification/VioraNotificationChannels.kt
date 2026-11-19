package pe.edu.upc.viora.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import pe.edu.upc.viora.R

/**
 * The notification channels of the app. The producer sees each one in the system settings and can turn it off
 * there; creating a channel that already exists only updates its name and description.
 */
object VioraNotificationChannels {

    /** The morning summary of the day of Inicio (P110). */
    const val DAILY_SUMMARY = "viora_daily_summary"

    const val CRITICAL_ALERTS = "viora_critical_alerts"

    fun ensureCriticalAlerts(context: Context) {
        val channel = NotificationChannel(CRITICAL_ALERTS, context.getString(R.string.notification_channel_critical_alerts), NotificationManager.IMPORTANCE_HIGH).apply {
            description = context.getString(R.string.notification_channel_critical_alerts_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * Whether a critical alert would reach the producer: app notifications on, the Android 13+ permission
     * granted and the channel not turned off in the system settings.
     */
    fun canPostCriticalAlerts(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        ensureCriticalAlerts(context)
        return context.getSystemService(NotificationManager::class.java)
            .getNotificationChannel(CRITICAL_ALERTS).importance != NotificationManager.IMPORTANCE_NONE
    }

    fun ensureDailySummary(context: Context) {
        val channel = NotificationChannel(
            DAILY_SUMMARY,
            context.getString(R.string.notification_channel_daily_summary),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.notification_channel_daily_summary_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
