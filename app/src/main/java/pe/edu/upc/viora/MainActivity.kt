package pe.edu.upc.viora

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZoneId
import javax.inject.Inject
import pe.edu.upc.viora.core.locale.AppLanguage
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.core.presentation.LocalAppZone

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var zone: ZoneId

    // Before Android 13 the language chosen in Mi cuenta is applied here (from 13 the system does it).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The manifest opens with the green launch window of the splash; the app itself is cream.
        setTheme(R.style.Theme_Viora)
        super.onCreate(savedInstanceState)

        // On Android 12+ the system shows a mandatory splash screen. On some emulator
        // configurations (notably API 37) the exit animation gets stuck, hiding the app
        // behind an opaque starting window. Dismissing it explicitly works around this.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { it.remove() }
        }

        // Light bars with dark icons always: the app is light-only, even if the phone is in dark mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            CompositionLocalProvider(LocalAppZone provides zone) {
                VioraTheme {
                    VioraApp()
                }
            }
        }
    }
}
