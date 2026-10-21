package pe.edu.upc.viora.features.iam.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import pe.edu.upc.viora.core.designsystem.theme.Forest
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Shadow
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500

// The two welcome drawings (Figma "Ilustración · Rama de olivo" and "Ilustración · Olivo", 300 × 300).
// The paths are the Figma vectors; they are drawn on a canvas instead of a static image so each
// stroke can be traced on entry and then keep a slow, barely-there sway, like the olive sprig of
// the verification email.

/** T02 · Bienvenida: an olive branch whose stem, leaves and olives are drawn in sequence. */
@Composable
internal fun OliveBranchIllustration(modifier: Modifier = Modifier, size: Dp = IllustrationSize) {
    val intro = rememberIntro(BRANCH_INTRO_MILLIS)
    val idle = rememberInfiniteTransition(label = "branch idle")
    val breath = idle.wave(BREATH_MILLIS)
    val sway = idle.wave(SWAY_MILLIS)
    val flutter = idle.loop(FLUTTER_MILLIS)
    val paths = remember { BranchPaths() }

    Canvas(modifier.size(size)) {
        val t = intro.value
        inViewport {
            drawBlob(paths.blob, appear = phase(t, 0f, 0.35f), breath = breath.value)
            // The whole branch sways from where it enters the frame.
            rotate(degrees = (sway.value - 0.5f) * 2.4f, pivot = BranchPivot) {
                drawTraced(paths.stem, phase(t, 0.08f, 0.55f), strokeWidth = 1.6f)
                paths.leaves.forEachIndexed { i, leaf ->
                    val grow = phase(t, 0.2f + i * 0.055f, 0.45f + i * 0.055f)
                    if (grow <= 0f) return@forEachIndexed
                    val angle = sin((flutter.value + i * 0.13f) * TWO_PI) * 2.5f
                    rotate(degrees = angle, pivot = leaf.base) {
                        scale(scale = 0.6f + 0.4f * grow, pivot = leaf.base) {
                            drawTraced(leaf.outline, grow, strokeWidth = 1.6f)
                            drawTraced(leaf.midrib, phase(grow, 0.4f, 1f), strokeWidth = 1f)
                        }
                    }
                }
                paths.olives.forEachIndexed { i, olive ->
                    val pop = phase(t, 0.68f + i * 0.08f, 0.9f + i * 0.04f)
                    if (pop <= 0f) return@forEachIndexed
                    val swing = sin((flutter.value + 0.3f + i * 0.21f) * TWO_PI) * 4f
                    rotate(degrees = swing, pivot = olive.stalkTop) {
                        drawTraced(olive.stalk, phase(pop, 0f, 0.5f), strokeWidth = 1.2f)
                        scale(scale = Overshoot.transform(phase(pop, 0.3f, 1f)), pivot = olive.stalkEnd) {
                            if (olive.fill != null) {
                                drawPath(olive.body, olive.fill)
                            } else {
                                drawPath(olive.body, Neutral900, style = line(1.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** T02 · Bienvenida / Lámina productor: an olive tree on its ground line, with the sun rising. */
@Composable
internal fun OliveTreeIllustration(modifier: Modifier = Modifier, size: Dp = IllustrationSize) {
    val intro = rememberIntro(TREE_INTRO_MILLIS)
    val idle = rememberInfiniteTransition(label = "tree idle")
    val breath = idle.wave(BREATH_MILLIS)
    val sway = idle.wave(SWAY_MILLIS)
    val glow = idle.wave(SUN_MILLIS)
    val paths = remember { TreePaths() }

    Canvas(modifier.size(size)) {
        val t = intro.value
        inViewport {
            drawBlob(paths.blob, appear = phase(t, 0f, 0.3f), breath = breath.value)

            // The sun rises from behind the crown and then breathes slowly.
            val rise = phase(t, 0.5f, 0.85f)
            if (rise > 0f) {
                translate(top = (1f - rise) * 18f) {
                    scale(scale = 1f + glow.value * 0.05f, pivot = SunCenter) {
                        drawPath(paths.sun, Shadow, alpha = rise)
                    }
                }
            }

            drawTraced(paths.ground, phase(t, 0.04f, 0.35f), strokeWidth = 1.4f, fromCenter = true)
            paths.trunk.forEach { drawTraced(it, phase(t, 0.18f, 0.45f), strokeWidth = 1.6f, reverse = true) }

            // The crown and its olives sway together over the trunk.
            rotate(degrees = (sway.value - 0.5f) * 2.4f, pivot = CrownPivot) {
                paths.boughs.forEach { drawTraced(it, phase(t, 0.32f, 0.5f), strokeWidth = 1.4f) }
                drawTraced(paths.crown, phase(t, 0.32f, 0.8f), strokeWidth = 1.6f)
                paths.olives.forEachIndexed { i, olive ->
                    val pop = Overshoot.transform(phase(t, 0.66f + i * 0.045f, 0.84f + i * 0.045f))
                    if (pop <= 0f) return@forEachIndexed
                    scale(scale = pop, pivot = olive.center) {
                        drawPath(olive.path, Shadow)
                    }
                }
                val hollow = Overshoot.transform(phase(t, 0.9f, 1f))
                if (hollow > 0f) {
                    scale(scale = hollow, pivot = paths.hollowOlive.center) {
                        drawPath(paths.hollowOlive.path, Neutral900, style = line(1.4f))
                    }
                }
            }
        }
    }
}

// ---- Drawing helpers ---------------------------------------------------------------------

/** Scales the 300 × 300 Figma viewport to the canvas. */
private inline fun DrawScope.inViewport(block: DrawScope.() -> Unit) {
    scale(scale = size.minDimension / VIEWPORT, pivot = Offset.Zero, block = block)
}

private fun DrawScope.drawBlob(blob: Path, appear: Float, breath: Float) {
    if (appear <= 0f) return
    scale(scale = (0.86f + 0.14f * FastOutSlowInEasing.transform(appear)) * (1f + breath * 0.025f), pivot = BlobCenter) {
        drawPath(blob, Green200, alpha = 0.75f * appear)
    }
}

/**
 * Draws [traced] up to [progress] of its length: from the start, from the end ([reverse]) or
 * outwards from the middle ([fromCenter]).
 */
private fun DrawScope.drawTraced(
    traced: TracedPath,
    progress: Float,
    strokeWidth: Float,
    reverse: Boolean = false,
    fromCenter: Boolean = false,
) {
    if (progress <= 0f) return
    val segment = if (progress >= 1f) {
        traced.path
    } else {
        val length = traced.length
        val (start, end) = when {
            fromCenter -> length * (0.5f - progress / 2f) to length * (0.5f + progress / 2f)
            reverse -> length * (1f - progress) to length
            else -> 0f to length * progress
        }
        Path().also { traced.measure.getSegment(start, end, it, startWithMoveTo = true) }
    }
    drawPath(segment, Neutral900, style = line(strokeWidth))
}

private fun line(width: Float) = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)

/** 0 before [start], 1 after [end], eased in between. */
private fun phase(t: Float, start: Float, end: Float): Float =
    FastOutSlowInEasing.transform(((t - start) / (end - start)).coerceIn(0f, 1f))

@Composable
private fun rememberIntro(durationMillis: Int): State<Float> {
    val intro = remember { Animatable(0f) }
    LaunchedEffect(Unit) { intro.animateTo(1f, tween(durationMillis, easing = LinearEasing)) }
    return intro.asState()
}

/** 0 → 1 → 0, eased, forever. */
@Composable
private fun InfiniteTransition.wave(periodMillis: Int): State<Float> = animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(periodMillis / 2, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "wave",
)

/** 0 → 1 linearly, forever (a phase for sine motions). */
@Composable
private fun InfiniteTransition.loop(periodMillis: Int): State<Float> = animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing)),
    label = "loop",
)

// ---- Figma paths -------------------------------------------------------------------------

private fun path(data: String): Path = PathParser().parsePathString(data).toPath()

/** A stroke that can be traced progressively. */
private class TracedPath(data: String) {
    val path = path(data)
    val measure = PathMeasure().apply { setPath(path, forceClosed = false) }
    val length = measure.length
}

private class Leaf(outline: String, midrib: String, val base: Offset) {
    val outline = TracedPath(outline)
    val midrib = TracedPath(midrib)
}

private class HangingOlive(stalk: String, body: String, val stalkTop: Offset, val stalkEnd: Offset, val fill: Color?) {
    val stalk = TracedPath(stalk)
    val body = path(body)
}

private class TreeOlive(data: String, val center: Offset) {
    val path = path(data)
}

private class BranchPaths {
    val blob = path("M140 48C205 30 262 78 258 148C254 222 200 266 136 258C74 250 38 206 42 146C46 90 82 62 140 48Z")
    val stem = TracedPath("M40 270C100 214 172 138 250 66")
    val leaves = listOf(
        Leaf(
            "M91.12 220.2C82.1426 209.598 79.2253 193.053 84.1741 180.808C93.0126 190.622 95.9299 207.167 91.12 220.2Z",
            "M90.7727 218.23L85.5633 188.686",
            Offset(91.12f, 220.2f),
        ),
        Leaf(
            "M106.8 205C119.242 200.086 134.96 202.857 144.223 211.599C132.529 216.645 116.811 213.873 106.8 205Z",
            "M108.77 205.347L136.738 210.279",
            Offset(106.8f, 205f),
        ),
        Leaf(
            "M130.32 182.2C119.364 171.327 114.894 153.396 119.675 139.507C130.418 149.526 134.889 167.457 130.32 182.2Z",
            "M129.836 180.259L121.804 148.046",
            Offset(130.32f, 182.2f),
        ),
        Leaf(
            "M146 167C159.367 160.361 176.911 162.205 187.77 171.39C175.238 178.117 157.695 176.273 146 167Z",
            "M147.989 167.209L179.416 170.512",
            Offset(146f, 167f),
        ),
        Leaf(
            "M169.52 144.2C160.918 133.291 158.58 116.654 163.953 104.589C172.444 114.706 174.782 131.343 169.52 144.2Z",
            "M169.242 142.219L165.066 112.511",
            Offset(169.52f, 144.2f),
        ),
        Leaf(
            "M185.2 129C198.537 125.111 214.838 129.175 224.012 138.677C211.451 142.759 195.15 138.695 185.2 129Z",
            "M187.141 129.484L216.249 136.741",
            Offset(185.2f, 129f),
        ),
        Leaf(
            "M208.72 106.2C202.72 96 202.72 81.72 208.72 72.2C214.72 81.72 214.72 96 208.72 106.2Z",
            "M208.72 104.2V79",
            Offset(208.72f, 106.2f),
        ),
        Leaf(
            "M224.4 91C235.473 88.6452 248.103 93.242 254.47 101.945C243.998 104.518 231.369 99.9216 224.4 91Z",
            "M226.279 91.684L248.456 99.7557",
            Offset(224.4f, 91f),
        ),
    )
    val olives = listOf(
        HangingOlive(
            "M83.28 227.8L79.98 238.8",
            "M73.6888 257.667C77.8406 259.178 82.8142 255.985 84.7975 250.536C86.7809 245.087 85.023 239.444 80.8712 237.933C76.7194 236.422 71.7458 239.615 69.7625 245.064C67.7791 250.513 69.537 256.156 73.6888 257.667Z",
            stalkTop = Offset(83.28f, 227.8f),
            stalkEnd = Offset(79.98f, 238.8f),
            fill = Forest,
        ),
        HangingOlive(
            "M157.76 155.6L164.36 165.5",
            "M173.351 183.467C177.503 181.956 179.261 176.313 177.278 170.864C175.294 165.415 170.321 162.222 166.169 163.733C162.017 165.244 160.259 170.887 162.242 176.336C164.226 181.785 169.199 184.978 173.351 183.467Z",
            stalkTop = Offset(157.76f, 155.6f),
            stalkEnd = Offset(164.36f, 165.5f),
            fill = Terracotta500,
        ),
        HangingOlive(
            "M216.56 98.6L224.26 107.4",
            "M237.81 111.219C240.261 116.475 239.002 122.249 234.997 124.116C230.993 125.983 225.76 123.237 223.31 117.981C220.859 112.725 222.118 106.951 226.123 105.084C230.127 103.217 235.36 105.963 237.81 111.219Z",
            stalkTop = Offset(216.56f, 98.6f),
            stalkEnd = Offset(224.26f, 107.4f),
            fill = null,
        ),
    )
}

private class TreePaths {
    val blob = path("M146 42C212 30 262 84 256 150C250 214 204 258 140 254C78 250 40 206 44 144C48 90 90 52 146 42Z")
    val crown = TracedPath(
        "M96 176C72 174 64 146 82 134C74 108 98 90 118 98C126 72 160 68 172 88C190 72 222 82 220 108C242 112 248 142 230 154C238 176 216 192 198 184C188 202 158 204 148 190C134 202 106 198 96 176Z",
    )
    val trunk = listOf(
        TracedPath("M148 190C152 214 142 238 144 266"),
        TracedPath("M162 188C160 216 168 240 166 266"),
    )
    val boughs = listOf(
        TracedPath("M150 206C138 196 126 192 116 190"),
        TracedPath("M160 200C172 192 184 190 194 184"),
    )
    val ground = TracedPath("M20 266H280")
    val olives = listOf(
        TreeOlive("M118 134C120.485 134 122.5 131.314 122.5 128C122.5 124.686 120.485 122 118 122C115.515 122 113.5 124.686 113.5 128C113.5 131.314 115.515 134 118 134Z", Offset(118f, 128f)),
        TreeOlive("M164 118C166.485 118 168.5 115.314 168.5 112C168.5 108.686 166.485 106 164 106C161.515 106 159.5 108.686 159.5 112C159.5 115.314 161.515 118 164 118Z", Offset(164f, 112f)),
        TreeOlive("M196 144C198.485 144 200.5 141.314 200.5 138C200.5 134.686 198.485 132 196 132C193.515 132 191.5 134.686 191.5 138C191.5 141.314 193.515 144 196 144Z", Offset(196f, 138f)),
        TreeOlive("M142 156C144.485 156 146.5 153.314 146.5 150C146.5 146.686 144.485 144 142 144C139.515 144 137.5 146.686 137.5 150C137.5 153.314 139.515 156 142 156Z", Offset(142f, 150f)),
        TreeOlive("M178 170C180.485 170 182.5 167.314 182.5 164C182.5 160.686 180.485 158 178 158C175.515 158 173.5 160.686 173.5 164C173.5 167.314 175.515 170 178 170Z", Offset(178f, 164f)),
    )
    val hollowOlive = TreeOlive(
        "M136.5 170C136.5 173.314 134.485 176 132 176C129.515 176 127.5 173.314 127.5 170C127.5 166.686 129.515 164 132 164C134.485 164 136.5 166.686 136.5 170Z",
        Offset(132f, 170f),
    )
    val sun = path("M232 88C244.15 88 254 78.1503 254 66C254 53.8497 244.15 44 232 44C219.85 44 210 53.8497 210 66C210 78.1503 219.85 88 232 88Z")
}

private const val VIEWPORT = 300f
private const val TWO_PI = (2 * Math.PI).toFloat()
private val IllustrationSize = 300.dp

private val BlobCenter = Offset(150f, 152f)
private val BranchPivot = Offset(40f, 270f)
private val CrownPivot = Offset(155f, 192f)
private val SunCenter = Offset(232f, 66f)

/** Ease that passes the target and comes back, for the olives popping in. */
private val Overshoot = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

private const val BRANCH_INTRO_MILLIS = 2200
private const val TREE_INTRO_MILLIS = 2200
private const val BREATH_MILLIS = 7000
private const val SWAY_MILLIS = 6000
private const val FLUTTER_MILLIS = 4200
private const val SUN_MILLIS = 5000
