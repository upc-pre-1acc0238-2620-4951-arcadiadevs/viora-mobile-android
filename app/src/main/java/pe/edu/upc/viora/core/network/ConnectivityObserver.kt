package pe.edu.upc.viora.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Whether the phone has a network that reaches the internet right now, so a screen can skip a send it would lose. */
fun interface ConnectivityObserver {
    fun isOnline(): Boolean
}

/**
 * The platform's answer. When the system cannot tell (no connectivity service, a failure asking it), it says
 * online and lets the request itself find out.
 */
class AndroidConnectivityObserver @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ConnectivityObserver {

    override fun isOnline(): Boolean = try {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (manager == null) {
            true
        } else {
            val network = manager.activeNetwork
            val capabilities = network?.let(manager::getNetworkCapabilities)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }
    } catch (_: Exception) {
        true
    }
}
