package pe.edu.upc.viora.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import pe.edu.upc.viora.R

/**
 * The notification channels of the app. The producer sees each one in the system settings and can turn it off
 * there; creating a channel that already exists only updates its name and description.
 */
object VioraNotificationChannels {

    /** The morning summary of the day of Inicio (P110). */
    const val DAILY_SUMMARY = "viora_daily_summary"

    fun ensureDailySummary(context: Context) {
        val channel = NotificationChannel(
            DAILY_SUMMARY,
            context.getString(R.string.notification_channel_daily_summary),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.notification_channel_daily_summary_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
