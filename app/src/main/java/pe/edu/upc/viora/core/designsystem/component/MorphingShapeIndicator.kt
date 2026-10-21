package pe.edu.upc.viora.core.designsystem.component

import android.graphics.Matrix
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.pill
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.graphics.shapes.transformed
import kotlin.math.min
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Shapes of the Material 3 expressive shape library ("Soft burst", "9-sided cookie", "Pentagon",
 * "Pill", "Sunny", "4-sided cookie", "Oval"), rebuilt with graphics-shapes because the Material 3
 * version of the app (1.4) does not ship them yet. All are normalized into the unit square.
 */
object MorphingShapes {
    val SoftBurst: RoundedPolygon = RoundedPolygon.star(10, innerRadius = 0.72f, rounding = CornerRounding(0.24f)).normalized()
    val Cookie9: RoundedPolygon = RoundedPolygon.star(9, innerRadius = 0.8f, rounding = CornerRounding(0.5f)).normalized()
    val Pentagon: RoundedPolygon = RoundedPolygon(5, rounding = CornerRounding(0.32f)).normalized()
    val Pill: RoundedPolygon = RoundedPolygon.pill(width = 2f, height = 1.3f).transformed(rotation(-45f)).normalized()
    val Sunny: RoundedPolygon = RoundedPolygon.star(8, innerRadius = 0.8f, rounding = CornerRounding(0.15f)).normalized()
    val Cookie4: RoundedPolygon = RoundedPolygon.star(4, innerRadius = 0.7f, rounding = CornerRounding(0.42f)).normalized()
    val Oval: RoundedPolygon = RoundedPolygon.circle(8).transformed(Matrix().apply { setScale(1f, 0.64f); postRotate(-45f) }).normalized()
    val Circle: RoundedPolygon = RoundedPolygon.circle(8).normalized()

    /** The order the loading indicator walks through, like the Material 3 one. */
    val Loading: List<RoundedPolygon> = listOf(SoftBurst, Cookie9, Pentagon, Pill, Sunny, Cookie4, Oval)

    private fun rotation(degrees: Float) = Matrix().apply { setRotate(degrees) }
}

/**
 * Material 3 expressive loading indicator: a filled shape that keeps morphing into the next one of
 * [shapes] while it turns. Each morph rides a bouncy spring and adds a quarter turn, on top of a slow
 * constant spin, as the Material 3 `LoadingIndicator` does.
 *
 * When [settle] becomes true the current morph ends in a circle and the indicator stops there;
 * [onSettled] runs once the circle is complete, so a screen can continue from it (the splash fills
 * its ring with it).
 */
@Composable
fun MorphingShapeIndicator(
    color: Color,
    modifier: Modifier = Modifier,
    shapes: List<RoundedPolygon> = MorphingShapes.Loading,
    settle: Boolean = false,
    onSettled: () -> Unit = {},
) {
    val morphs = remember(shapes) { shapes.indices.map { Morph(shapes[it], shapes[(it + 1) % shapes.size]) } }
    val toCircle = remember(shapes) { shapes.map { Morph(it, MorphingShapes.Circle) } }
    var index by remember { mutableIntStateOf(0) }
    var settling by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val stepTurn = remember { Animatable(0f) }
    val currentSettle by rememberUpdatedState(settle)
    val currentOnSettled by rememberUpdatedState(onSettled)

    LaunchedEffect(morphs) {
        while (true) {
            if (currentSettle) settling = true
            progress.snapTo(0f)
            coroutineScope {
                launch { progress.animateTo(1f, MorphSpring) }
                launch { stepTurn.animateTo(stepTurn.targetValue + 90f, MorphSpring) }
                // The next morph starts once this one has visually landed, not when the spring fully rests.
                // The last one, into the circle, hands over sooner so the screen can go on.
                delay(if (settling) SETTLE_MILLIS else MORPH_MILLIS)
            }
            if (settling) {
                currentOnSettled()
                return@LaunchedEffect
            }
            index = (index + 1) % shapes.size
        }
    }

    val spin by rememberInfiniteTransition(label = "indicator spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(SPIN_MILLIS, easing = LinearEasing)),
        label = "spin",
    )
    val path = remember { android.graphics.Path() }

    Canvas(modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }) {
        val morph = if (settling) toCircle[index] else morphs[index]
        path.rewind()
        morph.toPath(progress.value, path)
        val side = min(size.width, size.height)
        rotate(spin + stepTurn.value) {
            translate(left = (size.width - side) / 2f, top = (size.height - side) / 2f) {
                scale(scale = side, pivot = Offset.Zero) {
                    drawPath(path.asComposePath(), color)
                }
            }
        }
    }
}

private const val MORPH_MILLIS = 650L
private const val SETTLE_MILLIS = 450L
private const val SPIN_MILLIS = 4666
private val MorphSpring = spring<Float>(dampingRatio = 0.6f, stiffness = 200f)
