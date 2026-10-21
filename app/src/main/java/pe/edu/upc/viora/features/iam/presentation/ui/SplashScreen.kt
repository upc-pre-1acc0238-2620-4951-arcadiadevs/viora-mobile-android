package pe.edu.upc.viora.features.iam.presentation.ui

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.MorphingShapeIndicator
import pe.edu.upc.viora.core.designsystem.component.WavyCircularProgressIndicator
import pe.edu.upc.viora.core.designsystem.theme.Forest
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Shadow
import pe.edu.upc.viora.features.iam.presentation.viewmodel.SplashDestination
import pe.edu.upc.viora.features.iam.presentation.viewmodel.SplashViewModel

/**
 * T01 · Splash (Figma "00 · Splash"), played in this order on the brand green:
 * 1. a white ring holds a Material 3 loading shape that keeps morphing (the Figma ring filling up,
 *    in the dark brand color instead of yellow);
 * 2. the shape settles into a circle and fills the ring;
 * 3. ring and circle give way to the white isotype;
 * 4. the isotype shrinks into the "o" of "Viora" while the other letters appear;
 * 5. a wavy progress ring shows under the logo until the saved session has been read.
 */
@Composable
fun SplashScreen(
    onFinished: (SplashDestination) -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    var settle by remember { mutableStateOf(false) }
    var settled by remember { mutableStateOf(false) }
    val fill = remember { Animatable(0f) }
    val isotype = remember { Animatable(0f) }
    val wordmark = remember { Animatable(0f) }
    val loader = remember { Animatable(0f) }
    val leave = remember { Animatable(0f) }

    LightSystemBarIcons()

    LaunchedEffect(Unit) {
        delay(MORPH_PHASE_MILLIS)
        settle = true
        snapshotFlow { settled }.first { it }
        fill.animateTo(1f, tween(FILL_MILLIS, easing = EmphasizedDecelerate))
        isotype.animateTo(1f, tween(ISOTYPE_MILLIS, easing = EmphasizedDecelerate))
        wordmark.animateTo(1f, tween(WORDMARK_MILLIS, easing = Emphasized))
        loader.animateTo(1f, tween(LOADER_FADE_MILLIS))
        delay(MIN_LOADER_MILLIS)
        val destination = viewModel.destination.filterNotNull().first()
        leave.animateTo(1f, tween(LEAVE_MILLIS))
        currentOnFinished(destination)
    }

    val appName = stringResource(R.string.app_name)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Forest)
            .semantics { contentDescription = appName }
            .graphicsLayer { alpha = 1f - leave.value },
        contentAlignment = Alignment.Center,
    ) {
        // 1-3 · Ring with the morphing shape, which gives way to the isotype.
        Box(
            modifier = Modifier
                .size(RingSize)
                .graphicsLayer {
                    val out = isotype.value
                    alpha = 1f - out
                    scaleX = lerp(1f, 0.7f, out)
                    scaleY = scaleX
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = RingStroke.toPx()
                drawCircle(Neutral0, radius = size.minDimension / 2f - stroke / 2f, style = Stroke(stroke))
            }
            MorphingShapeIndicator(
                color = Shadow,
                settle = settle,
                onSettled = { settled = true },
                modifier = Modifier
                    .size(ShapeSize)
                    .graphicsLayer {
                        scaleX = lerp(1f, FILLED_SCALE, fill.value)
                        scaleY = scaleX
                    },
            )
        }

        // 3-4 · Isotype, which then travels into the "o" of the wordmark.
        Image(
            painter = painterResource(R.drawable.ic_isotype),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Neutral0),
            modifier = Modifier
                .size(IsotypeSize)
                .graphicsLayer {
                    val move = wordmark.value
                    alpha = isotype.value * (1f - ((move - 0.8f) / 0.2f).coerceIn(0f, 1f))
                    val grow = lerp(0.7f, 1f, isotype.value)
                    scaleX = grow * lerp(1f, MarkSize / IsotypeSize, move)
                    scaleY = scaleX
                    translationX = lerp(0f, MarkOffsetX.toPx(), move)
                    translationY = lerp(0f, MarkOffsetY.toPx(), move)
                },
        )

        // 4 · The wordmark: its "o" takes over from the isotype and the letters open up around it.
        Box(Modifier.size(width = WordmarkWidth, height = WordmarkHeight)) {
            Image(
                painter = painterResource(R.drawable.logo_wordmark_mark),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Neutral0),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = ((wordmark.value - 0.8f) / 0.2f).coerceIn(0f, 1f) },
            )
            Image(
                painter = painterResource(R.drawable.logo_wordmark_letters),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Neutral0),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val reveal = ((wordmark.value - 0.35f) / 0.65f).coerceIn(0f, 1f)
                        alpha = reveal
                        scaleX = lerp(0.92f, 1f, reveal)
                    },
            )
        }

        // 5 · Loading ring under the logo (Figma "T01 · Splash / 10").
        WavyCircularProgressIndicator(
            color = Neutral0,
            trackColor = Neutral0.copy(alpha = 0.24f),
            modifier = Modifier
                .align(LoaderAlignment)
                .size(LoaderSize)
                .graphicsLayer { alpha = loader.value },
        )
    }
}

/** White status and navigation bar icons while the splash is on screen; restores them afterwards. */
@Composable
private fun LightSystemBarIcons() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val lightStatus = controller?.isAppearanceLightStatusBars
        val lightNavigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (lightStatus != null) controller.isAppearanceLightStatusBars = lightStatus
            if (lightNavigation != null) controller.isAppearanceLightNavigationBars = lightNavigation
        }
    }
}

// Material 3 emphasized easing curves.
private val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

private const val MORPH_PHASE_MILLIS = 1000L
private const val FILL_MILLIS = 300
private const val ISOTYPE_MILLIS = 380
private const val WORDMARK_MILLIS = 650
private const val LOADER_FADE_MILLIS = 200
private const val MIN_LOADER_MILLIS = 450L
private const val LEAVE_MILLIS = 250

private val RingSize = 112.dp
private val RingStroke = 3.dp
private val ShapeSize = 64.dp

/** The circle fills the ring up to a thin gap, as in "T01 · Splash / 7". */
private const val FILLED_SCALE = 90f / 64f

/** Sizes from Figma: the isotype alone is 103 dp; the isologotype 185 × 111 dp. */
private val IsotypeSize = 103.dp
private val WordmarkWidth = 185.dp
private val WordmarkHeight = 111.dp

/**
 * The "o" inside the isologotype canvas (407.65 × 244.74) spans 179.3–252.0 × 97.7–171.6, so at
 * 185 dp wide it measures 33.3 dp and its center sits 5.4 dp right and 5.6 dp below the canvas center.
 */
private val MarkSize = 33.3.dp
private val MarkOffsetX = 5.4.dp
private val MarkOffsetY = 5.6.dp

/** Figma places the loading ring at 82 % of the screen height. */
private val LoaderAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = 0.63f)
private val LoaderSize = 48.dp
