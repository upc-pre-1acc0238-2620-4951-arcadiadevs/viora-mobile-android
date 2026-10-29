package pe.edu.upc.viora.core.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import java.time.ZoneId

/**
 * The time zone screens show instants in. MainActivity provides the one Hilt injects (TimeModule), so a
 * screen never reads the system zone on its own and a test can pin it.
 */
val LocalAppZone = staticCompositionLocalOf<ZoneId> { ZoneId.systemDefault() }
