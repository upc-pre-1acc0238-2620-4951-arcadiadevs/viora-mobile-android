package pe.edu.upc.viora.features.plotmanagement.infrastructure.location

import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationTracker
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix

/** [LocationTracker] on Google's fused provider, asking for the most precise fix every second. */
class FusedLocationTracker @Inject constructor(
    private val client: FusedLocationProviderClient,
) : LocationTracker {

    override fun observeFixes(): Flow<GpsFix> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                // A position without accuracy cannot be judged, so it is not used.
                if (!location.hasAccuracy()) return
                trySend(GpsFix(GeoPoint(location.latitude, location.longitude), location.accuracy.toDouble()))
            }
        }
        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnFailureListener { close(it) }
        } catch (denied: SecurityException) {
            close(denied)
        }
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private companion object {
        const val UPDATE_INTERVAL_MS = 1_000L
        const val MIN_UPDATE_INTERVAL_MS = 500L
    }
}
