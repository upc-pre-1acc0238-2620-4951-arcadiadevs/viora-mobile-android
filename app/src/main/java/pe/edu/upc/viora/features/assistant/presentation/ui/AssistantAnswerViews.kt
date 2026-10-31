package pe.edu.upc.viora.features.assistant.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.component.VioraVoiceState
import pe.edu.upc.viora.core.designsystem.component.rememberAnimationsOff
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.newsreader
import pe.edu.upc.viora.core.designsystem.theme.robotoMedium
import pe.edu.upc.viora.features.assistant.domain.entity.AnswerFact
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.entity.FactTone
import pe.edu.upc.viora.features.assistant.presentation.state.AskFailure
import pe.edu.upc.viora.features.assistant.presentation.state.AskedQuestion

/** Height of a bento card and of its skeleton, so the answer lands without moving the sheet (P116 → P117). */
private val BentoHeight = 172.dp

/** The stale answer is shown at this strength under its notice (P119). */
private const val STALE_ALPHA = 0.55f

/** The cards rise after the voice starts writing, one after the other. */
private const val FIRST_CARD_DELAY_MILLIS = 320L
private const val NEXT_CARD_DELAY_MILLIS = 110L

/** The question that was asked, in the dark chip of P116 and P117. */
@Composable
internal fun QuestionChip(question: AskedQuestion, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Green800)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(Neutral0.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(painterResource(question.icon.drawable()), contentDescription = null, tint = Neutral50, modifier = Modifier.size(20.dp))
        }
        Text(
            text = question.label ?: stringResource(R.string.assistant_question_fallback),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, lineHeight = 20.sp),
            color = Neutral50,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Viora reading the plot (P116): the brote thinking, two skeleton cards of the answer's exact size
 * that breathe, and what she is reading. TalkBack hears it once, politely.
 */
@Composable
internal fun ThinkingBody(caption: String, modifier: Modifier = Modifier) {
    val still = rememberAnimationsOff()
    val pulse = if (still) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "assistant-skeleton")
        transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 850), RepeatMode.Reverse),
            label = "assistant-skeleton-alpha",
        ).value
    }
    val description = stringResource(R.string.assistant_thinking_a11y)
    Column(
        modifier = modifier.semantics {
            liveRegion = LiveRegionMode.Polite
            contentDescription = description
        },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        VioraVoice(state = VioraVoiceState.Thinking)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) { SkeletonCard(alpha = pulse, modifier = Modifier.weight(1f)) }
        }
        Text(caption, style = MaterialTheme.typography.bodySmall, color = Neutral600)
    }
}

@Composable
private fun SkeletonCard(alpha: Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(BentoHeight)
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral100)
            .padding(18.dp)
            .alpha(alpha),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(width = 92.dp, height = 44.dp).clip(CircleShape).background(Neutral200))
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth(0.85f).height(12.dp).clip(CircleShape).background(Neutral200))
        Box(Modifier.width(72.dp).height(20.dp).clip(CircleShape).background(Neutral200))
    }
}

/**
 * An answer (P117, P122): the voice writes the text, then the two bento cards rise one after the
 * other, both of the same height, and the source closes it. A stale answer (P119) keeps everything
 * dimmed under the notice of the new data.
 */
@Composable
internal fun AnswerBody(answer: AssistantAnswer, modifier: Modifier = Modifier) {
    val dim by animateFloatAsState(if (answer.isStale) STALE_ALPHA else 1f, tween(320), label = "assistant-stale")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.graphicsLayer { alpha = dim }) {
            VioraVoice(state = VioraVoiceState.Ready(answer.text, fromAssistant = true), utteranceId = answer.id)
        }
        BentoRow(answer = answer, modifier = Modifier.graphicsLayer { alpha = dim })
        SourceLine(answer.sources.firstOrNull())
    }
}

@Composable
private fun BentoRow(answer: AssistantAnswer, modifier: Modifier = Modifier) {
    // The cards rise once per answer: back on the sheet, or after a rotation, they are already there.
    var entered by rememberSaveable(answer.id) { mutableStateOf(false) }
    val still = rememberAnimationsOff()
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        answer.facts.forEachIndexed { index, fact ->
            val progress = remember(answer.id, index) { Animatable(if (entered || still) 1f else 0f) }
            LaunchedEffect(answer.id, index) {
                if (progress.value < 1f) {
                    delay(FIRST_CARD_DELAY_MILLIS + index * NEXT_CARD_DELAY_MILLIS)
                    progress.animateTo(1f, spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow))
                }
                entered = true
            }
            val rise = with(LocalDensity.current) { 22.dp.toPx() }
            BentoCard(
                fact = fact,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .graphicsLayer {
                        val p = progress.value
                        alpha = p.coerceIn(0f, 1f)
                        translationY = (1f - p) * rise
                        scaleX = 0.94f + 0.06f * p
                        scaleY = 0.94f + 0.06f * p
                    },
            )
        }
    }
}

/** A bento card (P117): the figure big in serif, what it is, and its pill at the bottom. */
@Composable
private fun BentoCard(fact: AnswerFact, modifier: Modifier = Modifier) {
    val primary = fact.tone == FactTone.PRIMARY
    val background = if (primary) Green800 else Harvest100
    val content = if (primary) Neutral50 else Neutral900
    Column(
        modifier = modifier
            .heightIn(min = BentoHeight)
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .padding(start = 18.dp, end = 14.dp, top = 16.dp, bottom = 16.dp)
            .semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = fact.value,
            style = newsreader(46.sp, 50.sp).copy(letterSpacing = (-0.9).sp),
            color = content,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = 46.sp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = fact.label,
            style = robotoMedium(13.sp, 17.sp),
            color = content.copy(alpha = 0.86f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f).heightIn(min = 12.dp))
        Text(
            text = fact.pill,
            style = robotoMedium(12.sp, 16.sp),
            color = if (primary) Neutral50 else Neutral900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (primary) Green900.copy(alpha = 0.55f) else Neutral0)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

/** "● Respuesta de Viora · basado en tus 4 campañas registradas". */
@Composable
private fun SourceLine(source: String?) {
    val text = if (source.isNullOrBlank()) {
        stringResource(R.string.assistant_answer_source_plain)
    } else {
        stringResource(R.string.assistant_answer_source, source.replaceFirstChar { it.lowercaseChar() })
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(Green800))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Neutral600)
    }
}

/** The yellow notice of the sheet: new data (P119) and no connection (P126). */
@Composable
internal fun AssistantNotice(text: String, modifier: Modifier = Modifier, @DrawableRes icon: Int? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Harvest100)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = Neutral900, modifier = Modifier.size(22.dp))
        } else {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Terracotta500))
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Neutral900, modifier = Modifier.weight(1f))
    }
}

/** Why the question was not answered (P127): the server failure in terracotta, the rest as a notice. */
@Composable
internal fun FailedBody(reason: AskFailure, modifier: Modifier = Modifier) {
    val text = when (reason) {
        AskFailure.Generic -> stringResource(R.string.assistant_error_generic)
        AskFailure.Offline -> stringResource(R.string.assistant_error_offline)
        is AskFailure.RateLimited -> reason.retryAfterMinutes
            ?.let { stringResource(R.string.assistant_error_rate_limited, it) }
            ?: stringResource(R.string.assistant_error_rate_limited_plain)
        AskFailure.Unavailable -> stringResource(R.string.assistant_error_unavailable)
        AskFailure.NoPlan -> stringResource(R.string.assistant_error_no_plan)
    }
    if (reason == AskFailure.Generic) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Terracotta100)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(painterResource(R.drawable.ic_priority_high), contentDescription = null, tint = Terracotta500, modifier = Modifier.size(24.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = Terracotta700, modifier = Modifier.weight(1f))
        }
    } else {
        AssistantNotice(text = text, modifier = modifier, icon = if (reason == AskFailure.Offline) R.drawable.ic_cloud_sync else null)
    }
}

/** The pair of buttons under an answer or an error: a filled one and an outlined one, 52 dp tall. */
@Composable
internal fun ButtonPair(
    primary: String,
    onPrimary: () -> Unit,
    secondary: String,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes primaryIcon: Int? = null,
    @DrawableRes secondaryIcon: Int? = null,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SheetPill(primary, onPrimary, filled = true, icon = primaryIcon, modifier = Modifier.weight(1f))
        SheetPill(secondary, onSecondary, filled = false, icon = secondaryIcon, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun SheetPill(
    text: String,
    onClick: () -> Unit,
    filled: Boolean,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
) {
    val content = if (filled) Neutral50 else Neutral900
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(CircleShape)
            .then(if (filled) Modifier.background(Green900) else Modifier.background(Neutral0).border(1.dp, Neutral300, CircleShape))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleSmall, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * "Volver a preguntar con datos nuevos" (P119): the dark 60 dp pill with the yellow circle, which
 * turns into a spinner while the new answer is written.
 */
@Composable
internal fun AskAgainButton(refreshing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(CircleShape)
            .background(Green900)
            .clickable(enabled = !refreshing, role = Role.Button, onClick = onClick)
            .padding(start = 24.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(R.string.assistant_ask_again),
            style = MaterialTheme.typography.titleMedium,
            color = Neutral50,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box(Modifier.size(44.dp).clip(CircleShape).background(Harvest300), contentAlignment = Alignment.Center) {
            if (refreshing) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Neutral900)
            } else {
                Icon(painterResource(R.drawable.ic_edit_note), contentDescription = null, tint = Neutral900)
            }
        }
    }
}

/** A row placeholder while the questions load: the same height as a question. */
@Composable
internal fun QuestionRowSkeleton(alpha: Float, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Neutral0)
            .padding(horizontal = 12.dp)
            .alpha(alpha)
            .clearAndSetSemantics {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Neutral100))
        Box(Modifier.fillMaxWidth(0.7f).height(14.dp).clip(CircleShape).background(Neutral100))
    }
}

/** Dot color of the overline of an answer: terracotta, as in P117 and P119. */
internal val OverlineDot: Color = Terracotta500
