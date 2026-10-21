package pe.edu.upc.viora.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Indeterminate circular progress with a wavy active arc, as the Material 3 expressive
 * `CircularWavyProgressIndicator`: the arc turns, grows and shrinks while its wave flows along it,
 * and the rest of the circle is a flat [trackColor] track separated by a small gap.
 */
@Composable
fun WavyCircularProgressIndicator(
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    amplitude: Dp = 1.6.dp,
    wavelength: Dp = 12.dp,
) {
    val transition = rememberInfiniteTransition(label = "wavy progress")
    val turn by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(TURN_MILLIS, easing = LinearEasing)),
        label = "turn",
    )
    val sweepFraction by transition.animateFloat(
        initialValue = MIN_SWEEP,
        targetValue = MAX_SWEEP,
        animationSpec = infiniteRepeatable(tween(SWEEP_MILLIS, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sweep",
    )
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(WAVE_MILLIS, easing = LinearEasing)),
        label = "wave phase",
    )
    val wave = remember { Path() }

    Canvas(modifier.semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }) {
        val stroke = strokeWidth.toPx()
        val amp = amplitude.toPx()
        val radius = min(size.width, size.height) / 2f - stroke / 2f - amp
        val center = Offset(size.width / 2f, size.height / 2f)
        val startDeg = turn - 90f
        val sweepDeg = sweepFraction * 360f
        // Gap between the arc and the track: one stroke width plus room for the round caps.
        val gapDeg = Math.toDegrees((stroke * 2f / radius).toDouble()).toFloat()

        drawArc(
            color = trackColor,
            startAngle = startDeg + sweepDeg + gapDeg,
            sweepAngle = (360f - sweepDeg - 2 * gapDeg).coerceAtLeast(0f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        wave.rewind()
        // Whole waves around the circle (so the wave does not jump when the turn restarts at 0°),
        // as many as keep it close to [wavelength] at any size.
        val wavesPerTurn = (2 * PI.toFloat() * radius / wavelength.toPx()).roundToInt().coerceAtLeast(1).toFloat()
        val steps = ceil(sweepDeg / 2f).toInt().coerceAtLeast(2)
        for (i in 0..steps) {
            val deg = startDeg + sweepDeg * i / steps
            val rad = Math.toRadians(deg.toDouble()).toFloat()
            val r = radius + amp * sin(rad * wavesPerTurn + phase)
            val point = Offset(center.x + r * cos(rad), center.y + r * sin(rad))
            if (i == 0) wave.moveTo(point.x, point.y) else wave.lineTo(point.x, point.y)
        }
        drawPath(wave, color, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

private const val TURN_MILLIS = 1500
private const val SWEEP_MILLIS = 1000
private const val WAVE_MILLIS = 1000
private const val MIN_SWEEP = 0.1f
private const val MAX_SWEEP = 0.72f
