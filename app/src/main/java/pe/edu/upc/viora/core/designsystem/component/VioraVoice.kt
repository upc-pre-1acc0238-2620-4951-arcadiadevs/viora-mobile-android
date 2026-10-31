package pe.edu.upc.viora.core.designsystem.component

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500

/** What Viora is doing while she speaks (Figma "Editorial/Voz de Viora", property Estado). */
sealed interface VioraVoiceState {

    /** Waiting for an answer (an assistant reply on its way): only the brote, turning. */
    data object Thinking : VioraVoiceState

    /** The text arrives in pieces (a streamed reply); it is written as it comes. */
    data class Streaming(val text: String) : VioraVoiceState

    /**
     * The whole text is known; it is written once and then stays. [fromAssistant] marks a reply of
     * the assistant, which is always written; a fixed text follows the producer's preference.
     */
    data class Ready(val text: String, val fromAssistant: Boolean = false) : VioraVoiceState
}

/**
 * The producer's preference "Voz de Viora animada" (Mi cuenta › Preferencias), provided once at the
 * root of the app. Off, fixed texts show complete with the isotype.
 */
val LocalVioraVoiceAnimated = staticCompositionLocalOf { true }

/**
 * Whether the voice writes letter by letter and the brote moves. The system setting that removes
 * animations always wins (it is an accessibility choice); otherwise the assistant's replies are
 * always written, because their text really is arriving, and fixed texts follow the preference.
 */
internal fun isVoiceAnimated(state: VioraVoiceState, preferenceOn: Boolean, systemAnimationsOff: Boolean): Boolean = when {
    systemAnimationsOff -> false
    state is VioraVoiceState.Thinking || state is VioraVoiceState.Streaming -> true
    state is VioraVoiceState.Ready && state.fromAssistant -> true
    else -> preferenceOn
}

/** One letter every 22 ms, about 45 a second (Figma spec "La voz escribe y el brote piensa"). */
private const val CHAR_MILLIS = 22L

/** The brote alternates Reposo and Pensando this often while writing. */
private const val BLINK_MILLIS = 220

/** The brote opens its yellow centre for this long when the last letter lands (Logro). */
private const val ACHIEVED_MILLIS = 280L

/** Cross-fade from the brote to the isotype (Listo). */
private const val SETTLE_MILLIS = 240

private enum class Phase { Thinking, Writing, Achieved, Done }

/**
 * Viora talking (Figma component "Editorial/Voz de Viora"): her mark next to one or two lines in
 * italic serif. The one place every screen takes it from, so the voice looks the same everywhere.
 */
@Composable
fun VioraVoice(text: String, modifier: Modifier = Modifier) =
    VioraVoice(state = VioraVoiceState.Ready(text), modifier = modifier)

/**
 * The voice with its animation: while the text is written letter by letter the brote thinks,
 * when the last letter lands it opens its yellow centre, and then the isotype takes its place.
 *
 * - The text is written once: back on the screen, or after a rotation, it is already whole.
 *   [utteranceId] names what is being said; it defaults to the text, and a streamed reply passes
 *   its own id so it is not written again when it turns [VioraVoiceState.Ready].
 * - The full height is laid out from the start, so the screen does not jump while it writes.
 * - Tapping the line completes it. Whether it animates at all is [isVoiceAnimated]: with the system
 *   animations off a streamed reply shows piece by piece as it arrives, with the brote still.
 * - TalkBack reads the whole text, never letter by letter.
 */
@Composable
fun VioraVoice(
    state: VioraVoiceState,
    modifier: Modifier = Modifier,
    utteranceId: Any? = null,
) {
    val target = when (state) {
        VioraVoiceState.Thinking -> ""
        is VioraVoiceState.Streaming -> state.text
        is VioraVoiceState.Ready -> state.text
    }
    val isFinal = state is VioraVoiceState.Ready
    val key = utteranceId ?: (state as? VioraVoiceState.Ready)?.text ?: "viora-voice"
    val animated = isVoiceAnimated(state, LocalVioraVoiceAnimated.current, rememberAnimationsOff())

    var shown by rememberSaveable(key) { mutableIntStateOf(if (animated) 0 else target.length) }
    var settled by rememberSaveable(key) { mutableStateOf(!animated && isFinal) }
    var achieved by remember(key) { mutableStateOf(false) }
    val latestTarget by rememberUpdatedState(target)
    val latestFinal by rememberUpdatedState(isFinal)
    val latestAnimated by rememberUpdatedState(animated)

    LaunchedEffect(key) {
        if (settled) return@LaunchedEffect
        while (true) {
            when {
                // Without animation the text that has arrived shows at once.
                !latestAnimated && shown < latestTarget.length -> shown = latestTarget.length
                shown < latestTarget.length -> {
                    delay(CHAR_MILLIS)
                    shown = (shown + 1).coerceAtMost(latestTarget.length)
                }
                latestFinal -> break
                // A stream that has nothing new yet: wait for more text or for its end.
                else -> snapshotFlow { latestTarget.length > shown || latestFinal }.first { it }
            }
        }
        if (latestAnimated) {
            achieved = true
            delay(ACHIEVED_MILLIS)
        }
        settled = true
    }

    val phase = when {
        settled -> Phase.Done
        achieved -> Phase.Achieved
        state is VioraVoiceState.Thinking -> Phase.Thinking
        else -> Phase.Writing
    }
    val visible = shown.coerceAtMost(target.length)
    val writing = phase == Phase.Writing && animated
    val thinkingLabel = stringResource(R.string.viora_voice_thinking)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = target.ifEmpty { thinkingLabel } }
            .clickable(
                enabled = isFinal && !settled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                shown = target.length
                settled = true
            },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        VioraVoiceMark(phase = phase, moving = animated, modifier = Modifier.padding(top = 2.dp).size(20.dp))

        if (phase == Phase.Thinking) {
            ThinkingDots(moving = animated)
        } else {
            var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
            val style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, lineHeight = 24.sp, fontStyle = FontStyle.Italic)
            // The unwritten letters are laid out but transparent, so the line keeps its final size.
            val text = buildAnnotatedString {
                append(target.substring(0, visible))
                withStyle(SpanStyle(color = Color.Transparent)) { append(target.substring(visible)) }
            }
            Text(
                text = text,
                style = style,
                color = Neutral700,
                onTextLayout = { layout = it },
                modifier = Modifier.drawWithContent {
                    drawContent()
                    val result = layout
                    if (writing && result != null) {
                        val cursor = result.getCursorRect(visible)
                        drawRect(
                            color = Terracotta500,
                            topLeft = Offset(cursor.left + 1.dp.toPx(), cursor.top + 3.dp.toPx()),
                            size = Size(2.dp.toPx(), cursor.height - 6.dp.toPx()),
                        )
                    }
                },
            )
        }
    }
}

/**
 * The mark of the voice. Thinking, the brote turns and breathes; writing, it alternates Reposo and
 * Pensando; at the end it opens its yellow centre (Logro) and fades into the isotype. Not [moving],
 * the brote stays still and the isotype appears without fading.
 */
@Composable
private fun VioraVoiceMark(phase: Phase, moving: Boolean, modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "brote")
    val turn by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1_600, easing = LinearEasing)),
        label = "turn",
    )
    val breath by motion.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath",
    )
    val blink by motion.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(durationMillis = BLINK_MILLIS * 2, easing = LinearEasing)),
        label = "blink",
    )

    // The pulse of Logro, then the brote gives way to the isotype.
    val pulse = remember { Animatable(1f) }
    val isotype = remember { Animatable(if (phase == Phase.Done) 1f else 0f) }
    LaunchedEffect(phase) {
        when (phase) {
            Phase.Achieved -> pulse.animateTo(
                1f,
                keyframes {
                    durationMillis = ACHIEVED_MILLIS.toInt()
                    1.12f at ACHIEVED_MILLIS.toInt() / 2
                },
            )
            Phase.Done -> if (moving) isotype.animateTo(1f, tween(SETTLE_MILLIS)) else isotype.snapTo(1f)
            else -> isotype.snapTo(0f)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isotype.value < 1f) {
            val brote = when (phase) {
                Phase.Thinking -> R.drawable.ic_brote_pensando
                Phase.Writing -> if (moving && blink >= 1f) R.drawable.ic_brote_pensando else R.drawable.ic_brote_reposo
                Phase.Achieved, Phase.Done -> R.drawable.ic_brote_logro
            }
            Image(
                painter = painterResource(brote),
                contentDescription = null,
                modifier = Modifier.size(20.dp).graphicsLayer {
                    val scale = when {
                        !moving -> 1f
                        phase == Phase.Thinking -> breath
                        phase == Phase.Achieved -> pulse.value
                        else -> 1f
                    }
                    scaleX = scale
                    scaleY = scale
                    rotationZ = when {
                        !moving -> 0f
                        phase == Phase.Thinking -> turn
                        phase == Phase.Writing -> turn / 4f
                        else -> 0f
                    }
                    alpha = 1f - isotype.value
                },
            )
        }
        if (isotype.value > 0f) {
            Image(
                painter = painterResource(R.drawable.ic_isotype),
                contentDescription = null,
                modifier = Modifier.size(20.dp).graphicsLayer {
                    alpha = isotype.value
                    val scale = 0.8f + 0.2f * isotype.value
                    scaleX = scale
                    scaleY = scale
                },
            )
        }
    }
}

/** "·  ·  ·" while an answer is on its way, each dot fading in turn (still when not [moving]). */
@Composable
private fun ThinkingDots(moving: Boolean) {
    val motion = rememberInfiniteTransition(label = "dots")
    val wave by motion.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1_200, easing = LinearEasing)),
        label = "wave",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 2.dp)) {
        repeat(3) { index ->
            val lit = if (!moving) 1f else (wave - index).let { if (it in 0f..1f) 1f - it else 0f }
            Text(
                text = "·",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, lineHeight = 24.sp),
                color = Neutral700.copy(alpha = 0.35f + 0.65f * lit),
            )
        }
    }
}

/** Whether the phone has the system animations off (Developer options or accessibility). */
@Composable
internal fun rememberAnimationsOff(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
