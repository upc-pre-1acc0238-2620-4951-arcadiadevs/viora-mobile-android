package pe.edu.upc.viora.features.telemetry.infrastructure.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import pe.edu.upc.viora.MainActivity
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.notification.VioraNotificationChannels
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertNotifier
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType
import pe.edu.upc.viora.features.telemetry.presentation.navigation.ALERT_DETAIL_LINK

class AndroidCriticalAlertNotifier @Inject constructor(@param:ApplicationContext private val context: Context) : CriticalAlertNotifier {
    override fun canNotify(): Boolean = VioraNotificationChannels.canPostCriticalAlerts(context)

    override fun show(incident: AgroclimaticIncident) {
        val uri = Uri.parse(ALERT_DETAIL_LINK).buildUpon().appendPath(incident.id).build()
        val intent = Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val tap = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val title = if (incident.type == IncidentType.FROST_WARNING) R.string.notification_frost_title else R.string.notification_critical_title
        val body = context.getString(R.string.notification_critical_body)
        val notification = NotificationCompat.Builder(context, VioraNotificationChannels.CRITICAL_ALERTS)
            .setSmallIcon(R.drawable.ic_isotype)
            .setContentTitle(context.getString(title, incident.plotName))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(incident.id, 0, notification)
    }
}
