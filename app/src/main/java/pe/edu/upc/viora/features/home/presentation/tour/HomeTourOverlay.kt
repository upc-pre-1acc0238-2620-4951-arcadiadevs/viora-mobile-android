package pe.edu.upc.viora.features.home.presentation.tour

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme

private val VeilColor = Color(0xFF12100E).copy(alpha = 0.74f)
private val CardShadow = Color(0xFF121A16)
private val FocusMargin = 8.dp
private val ScreenMargin = 16.dp
private val CardMargin = 24.dp
private val CardGap = 18.dp
private val FocusRadius = 24.dp

/** Texts of a stop of the tour. The title marks its italic part with `*asterisks*`. */
private class StopTexts(@StringRes val title: Int, @StringRes val body: Int)

private fun HomeTourTarget.texts(): StopTexts = when (this) {
    HomeTourTarget.Today -> StopTexts(R.string.tour_today_title, R.string.tour_today_body)
    HomeTourTarget.Phase -> StopTexts(R.string.tour_phase_title, R.string.tour_phase_body)
    HomeTourTarget.Field -> StopTexts(R.string.tour_field_title, R.string.tour_field_body)
    HomeTourTarget.Alternation -> StopTexts(R.string.tour_alternation_title, R.string.tour_alternation_body)
    HomeTourTarget.Plots -> StopTexts(R.string.tour_plots_title, R.string.tour_plots_body)
    HomeTourTarget.Register -> StopTexts(R.string.tour_register_title, R.string.tour_register_body)
}

/**
 * The first-visit tour of the Home (Figma P10 "Recorrido"): a welcome, one stop per section
 * that is on screen, and a closing card. Each stop dims the screen, rings the section and
 * explains it with Viora's voice. The sections report their position through
 * [homeTourTarget]; the overlay scrolls the Home so the section is in view.
 *
 * [onFinish] is called when the tour ends in any way (finished, skipped, "Ahora no" or back).
 */
@Composable
fun HomeTourOverlay(
    targets: HomeTourTargets,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The stops are fixed when the tour opens: sections that load later do not change its length.
    val stops = remember { HomeTourTarget.entries.filter { it in targets.rects.keys } }
    val lastStep = stops.size + 1
    var step by rememberSaveable { mutableIntStateOf(0) }
    var shownStep by remember { mutableStateOf<Int?>(null) }
    var focus by remember { mutableStateOf<Rect?>(null) }

    val density = LocalDensity.current
    val window = LocalWindowInfo.current.containerSize
    val screenWidth = window.width.toFloat()
    val screenHeight = window.height.toFloat()
    val statusTop = WindowInsets.statusBars.getTop(density).toFloat()
    val bottomInset = WindowInsets.navigationBars.getBottom(density).toFloat()
    val center = Offset(screenWidth / 2f, screenHeight / 2f)
    val hole = remember { Animatable(Rect(center, 0f), Rect.VectorConverter) }
    val ring = remember { Animatable(0f) }
    // Tour progress shown by the assistant's ring. It moves when a card appears, so the sweep is
    // seen (the card is hidden while the Home scrolls to the next section).
    var lastShown by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }
    val progressTarget = ((shownStep ?: lastShown) + 1f) / (lastStep + 1f)
    LaunchedEffect(shownStep) {
        shownStep?.let { lastShown = it }
        if (shownStep != null) progress.animateTo(progressTarget, tween(durationMillis = 700, easing = FastOutSlowInEasing))
    }

    BackHandler(onBack = onFinish)

    LaunchedEffect(step) {
        shownStep = null
        val target = stops.getOrNull(step - 1)
        if (target == null) {
            focus = null
            ring.animateTo(0f, tween(150))
            hole.animateTo(Rect(center, 0f), tween(300, easing = FastOutSlowInEasing))
            shownStep = step
            return@LaunchedEffect
        }
        ring.animateTo(0f, tween(100))
        // Bring the section near the top (the first stop goes back to the very top).
        val scroll = targets.scrollBy
        if (scroll != null && target != HomeTourTarget.Register) {
            val raw = targets.rects[target]
            if (raw != null) {
                val desiredTop = statusTop + with(density) { 60.dp.toPx() }
                val delta = if (target == HomeTourTarget.Today) -SCROLL_TO_TOP else raw.top - desiredTop
                scroll(delta)
            }
        }
        withFrameNanos { }
        val raw = targets.rects[target] ?: return@LaunchedEffect
        val margin = with(density) { FocusMargin.toPx() }
        val edge = with(density) { ScreenMargin.toPx() }
        val rect = Rect(
            left = maxOf(raw.left - margin, edge),
            top = raw.top - margin,
            right = minOf(raw.right + margin, screenWidth - edge),
            bottom = raw.bottom + margin,
        )
        focus = rect
        hole.animateTo(rect, tween(320, easing = FastOutSlowInEasing))
        ring.animateTo(1f, tween(200))
        shownStep = step
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Swallows every touch so the dimmed Home cannot be used during the tour.
            .pointerInput(Unit) { detectTapGestures { } },
    ) {
        Veil(hole = hole.value, ringAlpha = ring.value)

        val placedBelow = focus?.let { it.bottom + with(density) { (CardGap + CARD_ESTIMATED_HEIGHT).toPx() } <= screenHeight - bottomInset } ?: true
        val cardAlignment = when {
            focus == null -> Alignment.Center
            placedBelow -> Alignment.TopCenter
            else -> Alignment.BottomCenter
        }
        val cardPadding = focus?.let { rect ->
            with(density) {
                if (placedBelow) {
                    Modifier.padding(top = (rect.bottom).toDp() + CardGap)
                } else {
                    Modifier.padding(bottom = (screenHeight - rect.top).toDp() + CardGap)
                }
            }
        } ?: Modifier

        AnimatedVisibility(
            visible = shownStep != null,
            modifier = Modifier.align(cardAlignment),
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { if (placedBelow) it / 12 else -it / 12 },
            exit = fadeOut(tween(120)) + slideOutVertically(tween(120)) { 0 },
        ) {
            val current = shownStep ?: step
            val pointerX = focus?.center?.x?.let { with(density) { it.toDp() } }
            VoiceCard(
                step = current,
                lastStep = lastStep,
                stopCount = stops.size,
                progress = progress.value,
                texts = stops.getOrNull(current - 1)?.texts(),
                pointer = pointerX?.let { CardPointer(it, pointsUp = placedBelow) },
                onNext = { if (current >= lastStep) onFinish() else step = current + 1 },
                onSkip = onFinish,
                modifier = cardPadding.padding(horizontal = CardMargin),
            )
        }
    }
}

private const val SCROLL_TO_TOP = 100_000f
private val CARD_ESTIMATED_HEIGHT = 300.dp

/** The dimmed screen with a rounded window onto the section in focus, and the glowing ring around it. */
@Composable
private fun Veil(hole: Rect, ringAlpha: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val radius = CornerRadius(FocusRadius.toPx())
        val veil = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, size))
            if (hole.width > 0f && hole.height > 0f) addRoundRect(RoundRect(hole, radius))
        }
        drawPath(veil, VeilColor)
        if (ringAlpha > 0f && hole.width > 0f) {
            val inset = 1.dp.toPx()
            val topLeft = Offset(hole.left + inset, hole.top + inset)
            val ringSize = Size(hole.width - inset * 2, hole.height - inset * 2)
            val inner = CornerRadius(FocusRadius.toPx() - inset)
            drawRoundRect(Harvest300.copy(alpha = 0.18f * ringAlpha), topLeft, ringSize, inner, style = Stroke(width = 9.dp.toPx()))
            drawRoundRect(Harvest300.copy(alpha = ringAlpha), topLeft, ringSize, inner, style = Stroke(width = 2.dp.toPx()))
        }
    }
}

/** Where the card's little arrow sits: [x] is the horizontal centre of the section (in screen dp). */
private class CardPointer(val x: Dp, val pointsUp: Boolean)

@Composable
private fun VoiceCard(
    step: Int,
    lastStep: Int,
    stopCount: Int,
    progress: Float,
    texts: StopTexts?,
    pointer: CardPointer?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWelcome = step == 0
    val isDone = step == lastStep
    val shape = RoundedCornerShape(28.dp)
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 14.dp, shape = shape, ambientColor = CardShadow, spotColor = CardShadow)
                .clip(shape)
                .background(Neutral0)
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val signature = when {
                isWelcome -> stringResource(R.string.tour_signature_welcome)
                isDone -> stringResource(R.string.tour_signature_done)
                else -> stringResource(R.string.tour_signature_step, step, stopCount)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    VioraMark(progress = progress)
                    Text(
                        text = signature,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 1.4.sp),
                        color = Neutral600,
                    )
                }
                if (!isWelcome && !isDone) {
                    Text(
                        text = stringResource(R.string.tour_skip),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, letterSpacing = 0.sp),
                        color = Neutral600,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onSkip)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                    )
                }
            }

            val titleRes = when {
                isWelcome -> R.string.tour_welcome_title
                isDone -> R.string.tour_done_title
                else -> texts?.title
            }
            val bigTitle = isWelcome || isDone
            if (titleRes != null) {
                Text(
                    text = italicText(stringResource(titleRes)),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = if (bigTitle) 30.sp else 26.sp,
                        lineHeight = if (bigTitle) 34.sp else 30.sp,
                        letterSpacing = 0.sp,
                    ),
                    color = Neutral900,
                )
            }
            val body = when {
                isWelcome -> pluralStringResource(R.plurals.tour_welcome_body, stopCount, stopCount)
                isDone -> stringResource(R.string.tour_done_body)
                else -> texts?.let { stringResource(it.body) }
            }
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                    color = Neutral600,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = if (isDone) Arrangement.End else Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    isWelcome -> {
                        Text(
                            text = stringResource(R.string.tour_not_now),
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.sp),
                            color = Neutral600,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = onSkip)
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                        )
                        TourPillButton(text = stringResource(R.string.tour_start), onClick = onNext)
                    }
                    isDone -> TourPillButton(text = stringResource(R.string.tour_finish), onClick = onNext)
                    else -> {
                        StepDots(current = step, count = stopCount)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Green900)
                                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.tour_next), onClick = onNext),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral0)
                        }
                    }
                }
            }
        }

        if (pointer != null && !isWelcome && !isDone) {
            // x is in screen dp; the card starts CardMargin from the edge, the arrow is 22 dp wide.
            val arrowX = pointer.x - CardMargin - 11.dp
            Canvas(
                modifier = Modifier
                    .align(if (pointer.pointsUp) Alignment.TopStart else Alignment.BottomStart)
                    .offset(x = arrowX, y = if (pointer.pointsUp) (-10).dp else 10.dp)
                    .requiredSize(width = 22.dp, height = 11.dp)
                    .rotate(if (pointer.pointsUp) 0f else 180f),
            ) {
                val arrow = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(arrow, Neutral0)
            }
        }
    }
}

@Composable
private fun TourPillButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Green900)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, letterSpacing = 0.sp),
            color = Neutral0,
        )
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Harvest300),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral900)
        }
    }
}

@Composable
private fun StepDots(current: Int, count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { index ->
            val active = index + 1 == current
            Box(
                modifier = Modifier
                    .size(width = if (active) 22.dp else 6.dp, height = 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (active) Green900 else Green200),
            )
        }
    }
}

/** Turns `text *with emphasis*` into text whose starred part is in the italic display face. */
private fun italicText(text: String): AnnotatedString = buildAnnotatedString {
    text.split('*').forEachIndexed { index, part ->
        if (index % 2 == 1) withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(part) } else append(part)
    }
}

// Viora's mark as the assistant (Figma "Viora asistente"): a white disc with the isotype and a
// thin harvest ring. Path data copied from the exported vector (60 x 60 box, disc 36 dp at 12, 9.75).
private const val ISOTYPE_PATH = "M29.6903 18.7601C29.911 18.7469 30.1647 18.7461 30.3898 18.7608C30.3942 18.7498 30.4001 18.7498 30.4067 18.7608C30.5222 18.766 30.6384 18.7711 30.756 18.7762C30.7568 18.7674 30.7575 18.7601 30.759 18.7601C31.3716 18.7483 32.1329 18.9207 32.7308 19.1071C34.3003 19.5957 35.6411 20.5449 36.7024 21.6864C38.116 23.1742 38.9662 25.2172 39 27.2148V28.3417C38.936 32.576 35.3079 36.3232 31.0429 36.6629C30.467 36.7091 29.6992 36.7084 29.1218 36.6673C27.6737 36.5639 26.2439 36.076 25.0333 35.2793C23.7624 34.3924 22.6761 33.2516 21.9509 31.8519C21.3618 30.7148 21.0411 29.4699 21.0102 28.2397C20.9977 27.7606 20.9897 27.1862 21.0294 26.691C21.111 25.6735 21.4059 24.469 22.0598 23.6671C23.0696 22.4296 25.0664 21.8779 26.5484 22.4949C27.0632 22.7091 27.6207 23.1206 27.6722 23.709C26.7337 23.9987 25.807 24.3098 25.037 24.9348C24.9009 25.0456 24.7737 25.1674 24.67 25.3089C25.4268 24.7859 26.2917 24.3465 27.2199 24.3002C29.2799 24.2599 31.9004 24.3531 33.014 26.479C33.3449 27.1415 33.656 27.7929 33.9473 28.4334C34.2157 28.9689 34.5702 29.4567 34.9931 29.8815C35.0152 29.9028 35.0314 29.9226 35.0284 29.9387C35.0159 30.004 34.4312 30.106 34.315 30.125C33.9245 30.1881 33.3287 30.2109 32.9397 30.1382C31.9071 29.9453 31.3378 29.4149 30.6376 28.685L29.4167 27.2479C28.8548 26.6015 28.2231 26.0139 27.5074 25.54C27.4067 25.4755 27.4766 25.4505 27.5781 25.4256C27.9855 25.3273 28.7129 25.4432 29.1174 25.5591C30.4376 25.9391 31.5099 26.8282 32.1821 28.0189C31.4555 25.9618 29.0998 25.0023 27.0412 24.9722C26.2777 24.9612 25.6202 25.1226 25.0966 25.7088C24.403 26.4849 24.3044 27.5904 24.4133 28.5925C24.5986 30.3018 25.4415 31.6018 26.8632 32.5635C28.4555 33.6691 30.7435 33.8444 32.4785 32.9897C34.1297 32.1762 35.2991 30.5828 35.5852 28.7708C35.7485 27.443 35.6448 26.1372 34.9593 24.8937C34.3584 23.8036 33.3832 22.9035 32.2351 22.4112C30.9031 21.8397 29.3601 21.8405 27.9929 22.2865C26.6447 21.1531 24.6472 21.2169 23.1549 22.0107C23.0056 22.0591 23.0019 22.0422 23.0975 21.9116C24.6928 20.0167 27.2272 18.7293 29.6903 18.7601Z"
// The ring of the mark centres on the disc (30, 27.75 in the 60 x 60 art) and is 39 units wide.
private const val RING_RADIUS = 19.5f
private const val RING_WIDTH = 1.875f

/**
 * Viora as the assistant (Figma "Viora asistente"): a white disc with the isotype. The harvest
 * ring around it doubles as the progress of the tour: a faint track and an arc that grows
 * clockwise from the top, with a bead at its tip. [progress] runs from 0 to 1.
 */
@Composable
private fun VioraMark(progress: Float, modifier: Modifier = Modifier) {
    val isotype = remember { PathParser().parsePathString(ISOTYPE_PATH).toPath() }
    Box(modifier = modifier.size(36.dp)) {
        // requiredSize centres the 60 dp art on the 36 dp box; the disc sits 2.25 dp above the art's centre.
        Canvas(modifier = Modifier.requiredSize(60.dp).offset(y = 2.25.dp)) {
            val unit = size.width / 60f
            withTransform({ scale(unit, unit, pivot = Offset.Zero) }) {
                val center = Offset(30f, 27.75f)
                drawCircle(Neutral0, radius = 18f, center = center)
                drawPath(isotype, Green800)
                val topLeft = Offset(center.x - RING_RADIUS, center.y - RING_RADIUS)
                val ringSize = Size(RING_RADIUS * 2, RING_RADIUS * 2)
                drawArc(Harvest300.copy(alpha = 0.28f), 0f, 360f, useCenter = false, topLeft = topLeft, size = ringSize, style = Stroke(width = RING_WIDTH))
                val sweep = 360f * progress.coerceIn(0f, 1f)
                drawArc(Harvest300, -90f, sweep, useCenter = false, topLeft = topLeft, size = ringSize, style = Stroke(width = RING_WIDTH, cap = StrokeCap.Round))
                val angle = Math.toRadians((-90f + sweep).toDouble())
                val tip = Offset(center.x + RING_RADIUS * kotlin.math.cos(angle).toFloat(), center.y + RING_RADIUS * kotlin.math.sin(angle).toFloat())
                drawCircle(Harvest300, radius = 2.4f, center = tip)
                drawCircle(Neutral0, radius = 0.9f, center = tip)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF332F2B, widthDp = 402)
@Composable
private fun VoiceCardPreview() {
    VioraTheme {
        VoiceCard(
            step = 1,
            lastStep = 7,
            stopCount = 6,
            progress = 0.4f,
            texts = StopTexts(R.string.tour_today_title, R.string.tour_today_body),
            pointer = CardPointer(201.dp, pointsUp = true),
            onNext = {},
            onSkip = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF332F2B, widthDp = 402)
@Composable
private fun WelcomeCardPreview() {
    VioraTheme {
        VoiceCard(step = 0, lastStep = 7, stopCount = 6, progress = 0.15f, texts = null, pointer = null, onNext = {}, onSkip = {}, modifier = Modifier.padding(24.dp))
    }
}
