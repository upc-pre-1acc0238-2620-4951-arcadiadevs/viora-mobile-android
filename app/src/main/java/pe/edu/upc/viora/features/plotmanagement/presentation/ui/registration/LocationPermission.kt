package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * Whether the app may use the precise location, and the way to ask for it.
 *
 * [isBlocked] means Android will not show its permission dialog again (the producer denied it
 * twice, or chose "don't ask again"), so the only way back is the app's settings screen.
 */
@Stable
class LocationPermission internal constructor(
    val isGranted: Boolean,
    val isBlocked: Boolean,
    private val onRequest: (onResult: (Boolean) -> Unit) -> Unit,
    private val onOpenSettings: () -> Unit,
) {
    /** Shows the system dialog; [onResult] gets whether the precise location ended up granted. */
    fun request(onResult: (Boolean) -> Unit = {}) = onRequest(onResult)

    fun openSettings() = onOpenSettings()
}

/** Remembers the location permission. It is checked again when the app returns from the settings. */
@Composable
fun rememberLocationPermission(): LocationPermission {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasPreciseLocation(context)) }
    var blocked by remember { mutableStateOf(false) }
    var pendingResult by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    // The producer may have changed the permission in the settings and come back.
    LifecycleResumeEffect(Unit) {
        granted = hasPreciseLocation(context)
        if (granted) blocked = false
        onPauseOrDispose { }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        // After a denial Android says "show a rationale" once; when it no longer does, it will not ask again.
        val activity = context.findActivity()
        blocked = !granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        pendingResult?.invoke(granted)
        pendingResult = null
    }

    return remember(granted, blocked, launcher) {
        LocationPermission(
            isGranted = granted,
            isBlocked = blocked,
            onRequest = { onResult ->
                pendingResult = onResult
                launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            },
        )
    }
}

private fun hasPreciseLocation(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
