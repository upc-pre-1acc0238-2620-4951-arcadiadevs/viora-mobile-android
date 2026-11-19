package pe.edu.upc.viora.features.plotmanagement.infrastructure.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationService

class AndroidLocationService @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationService {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun isEnabled(): Boolean = LocationManagerCompat.isLocationEnabled(manager)

    override fun observeEnabled(): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(isEnabled())
            }
        }
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        trySend(isEnabled())
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}
