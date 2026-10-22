package pe.edu.upc.viora.features.home.presentation.tour

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize

/**
 * The places of the Home a tour can point at; each tour lists its own order. A stop whose section
 * is not on screen (e.g. a feature that is not integrated yet) is simply skipped.
 */
enum class HomeTourTarget {
    /** Headline, context chips and week. */
    Today,

    /** The phase card (countdown to the next intervention). */
    Phase,

    /** "Hoy en tu campo": weather, active alerts and soil moisture. */
    Field,

    /** "Tu alternancia": harvest by campaign. */
    Alternation,

    /** "Mis lotes": the carousel of plot cards. */
    Plots,

    /** "Primer paso": the card that invites an account without plots to draw its first one. */
    FirstPlot,

    /** The tab bar with the "+" button. */
    Register,
}

/**
 * Where each [HomeTourTarget] currently is, in window coordinates, and how to scroll the Home.
 * Sections mark themselves with [homeTourTarget]; the overlay reads the rectangles. Rectangles
 * keep following their section while the Home scrolls.
 */
@Stable
class HomeTourTargets {
    val rects = mutableStateMapOf<HomeTourTarget, Rect>()

    /** Scrolls the Home content by the given pixels (positive = content moves up). Set by the Home. */
    var scrollBy: (suspend (Float) -> Float)? = null
}

val LocalHomeTourTargets = compositionLocalOf<HomeTourTargets?> { null }

/** Marks this composable as the place the tour points at for [target]. */
fun Modifier.homeTourTarget(target: HomeTourTarget): Modifier = composed {
    val targets = LocalHomeTourTargets.current
    if (targets == null) {
        this
    } else {
        DisposableEffect(targets, target) { onDispose { targets.rects.remove(target) } }
        this.onGloballyPositioned { coordinates ->
            targets.rects[target] = Rect(coordinates.positionInWindow(), coordinates.size.toSize())
        }
    }
}
