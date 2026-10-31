package pe.edu.upc.viora.features.assistant.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.VioraBottomSheet
import pe.edu.upc.viora.core.designsystem.component.rememberAnimationsOff
import pe.edu.upc.viora.core.designsystem.theme.EditorialEyebrow
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.ShadowTint
import pe.edu.upc.viora.core.designsystem.theme.newsreader
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.core.presentation.formatDayMonth
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantQuestion
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.presentation.state.AnswerAge
import pe.edu.upc.viora.features.assistant.presentation.state.AskFailure
import pe.edu.upc.viora.features.assistant.presentation.state.AskPage
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantPresenter
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantSheetUiState
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantTab
import pe.edu.upc.viora.features.assistant.presentation.state.Catalog
import pe.edu.upc.viora.features.assistant.presentation.state.RecentAnswer
import pe.edu.upc.viora.features.assistant.presentation.viewmodel.AssistantSheetViewModel
import java.time.Instant

/**
 * The assistant sheet of a screen (Figma section 23). The view model lives with the screen, so
 * closing the sheet while Viora thinks does not lose the answer, and opening it again lands on it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantSheet(
    screen: AssistantScreen,
    plotId: String,
    plotName: String,
    onDismiss: () -> Unit,
    viewModel: AssistantSheetViewModel = hiltViewModel(key = "assistant-${screen.name}-$plotId"),
) {
    LaunchedEffect(screen, plotId) { viewModel.bind(screen, plotId, plotName) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val current = state ?: return
    VioraBottomSheet(onDismissRequest = onDismiss) {
        AssistantSheetContent(
            state = current,
            actions = AssistantSheetActions(
                onClose = onDismiss,
                onTab = viewModel::selectTab,
                onAsk = viewModel::ask,
                onRetry = viewModel::retry,
                onAnotherQuestion = viewModel::anotherQuestion,
                onAskAgain = viewModel::askAgainWithNewData,
                onOpenRecent = viewModel::openRecent,
                onReloadQuestions = viewModel::reloadQuestions,
            ),
        )
    }
}

/** What the producer can do on the sheet. */
data class AssistantSheetActions(
    val onClose: () -> Unit = {},
    val onTab: (AssistantTab) -> Unit = {},
    val onAsk: (AssistantQuestion) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onAnotherQuestion: () -> Unit = {},
    val onAskAgain: () -> Unit = {},
    val onOpenRecent: (RecentAnswer) -> Unit = {},
    val onReloadQuestions: () -> Unit = {},
)

/** The content of the sheet, stateless: header, tabs where the design has them, and the page. */
@Composable
fun AssistantSheetContent(
    state: AssistantSheetUiState,
    actions: AssistantSheetActions,
    modifier: Modifier = Modifier,
    now: Instant = Instant.now(),
) {
    val onQuestion = state.page is AskPage.Thinking || state.page is AskPage.Answered || state.page is AskPage.Failed
    val showTabs = state.tab == AssistantTab.RECENT || !onQuestion
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 24.dp)
            .navigationBarsPadding(),
    ) {
        Header(state = state, onQuestion = onQuestion, now = now, onClose = actions.onClose)
        if (showTabs) {
            Spacer(Modifier.height(20.dp))
            Tabs(selected = state.tab, onSelect = actions.onTab)
        }
        Spacer(Modifier.height(20.dp))
        val pageKey = when {
            state.tab == AssistantTab.RECENT -> "recent"
            state.page is AskPage.Thinking -> "question-${state.page.question.id}"
            state.page is AskPage.Answered -> "question-${state.page.question.id}"
            state.page is AskPage.Failed -> "question-${state.page.question.id}"
            state.page is AskPage.Opening -> "opening"
            else -> "questions"
        }
        AnimatedContent(
            targetState = pageKey,
            transitionSpec = {
                (fadeIn(tween(240, delayMillis = 60)) + slideInVertically(tween(280)) { it / 14 })
                    .togetherWith(fadeOut(tween(140)))
                    .using(SizeTransform(clip = false) { _, _ -> spring(stiffness = Spring.StiffnessMediumLow) })
            },
            label = "assistant-page",
        ) { key ->
            when {
                key == "recent" -> RecentPage(state.recent, now, actions)
                key == "opening" -> QuestionsLoading()
                key == "questions" -> QuestionsPage(state, actions)
                else -> QuestionPage(state, actions)
            }
        }
    }
}

// ---- Header ------------------------------------------------------------------------------------------------------

@Composable
private fun Header(state: AssistantSheetUiState, onQuestion: Boolean, now: Instant, onClose: () -> Unit) {
    val answered = (state.page as? AskPage.Answered)?.takeIf { state.tab == AssistantTab.ASK }
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f).padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (answered != null) Overline(answered.answer.createdAt, answered.answer.isStale, now)
            val titleStyle = newsreader(30.sp, 36.sp).copy(letterSpacing = (-0.3).sp)
            when {
                state.tab == AssistantTab.RECENT ->
                    Text(stringResource(R.string.assistant_title_recent), style = titleStyle, color = Neutral900, modifier = Modifier.semantics { heading() })
                onQuestion ->
                    Text(stringResource(R.string.assistant_title_answer), style = titleStyle, color = Neutral900, modifier = Modifier.semantics { heading() })
                else -> PlotTitle(state.plotName, titleStyle)
            }
        }
        Spacer(Modifier.width(12.dp))
        CircleIconButton(
            icon = R.drawable.ic_close,
            contentDescription = stringResource(R.string.assistant_close),
            onClick = onClose,
            background = Neutral100,
        )
    }
}

/** "¿Qué quieres saber de *La Yarada 02*?": the plot name in italic, wherever the language puts it. */
@Composable
private fun PlotTitle(plotName: String, style: androidx.compose.ui.text.TextStyle) {
    val marker = "\u0001"
    val template = stringResource(R.string.assistant_sheet_title, marker)
    val before = template.substringBefore(marker)
    val after = template.substringAfter(marker, "")
    Text(
        text = buildAnnotatedString {
            append(before)
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(plotName) }
            append(after)
        },
        style = style,
        color = Neutral900,
        modifier = Modifier.semantics { heading() },
    )
}

/** "● HACE 2 MIN · SE MANTIENE HASTA “OTRA PREGUNTA”" (P117) or "● RESPUESTA DEL 23 OCT" (P119). */
@Composable
private fun Overline(createdAt: Instant, stale: Boolean, now: Instant) {
    val zone = LocalAppZone.current
    val text = when (val age = AssistantPresenter.ageOf(createdAt, now, zone)) {
        is AnswerAge.OnDay -> stringResource(R.string.assistant_age_day, formatDayMonth(age.date))
        else -> {
            val ago = when (age) {
                AnswerAge.JustNow -> stringResource(R.string.assistant_age_now)
                is AnswerAge.Minutes -> stringResource(R.string.assistant_age_minutes, age.minutes)
                is AnswerAge.Hours -> stringResource(R.string.assistant_age_hours, age.hours)
                is AnswerAge.OnDay -> ""
            }
            if (stale) ago else "$ago · ${stringResource(R.string.assistant_keeps_until)}"
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(OverlineDot))
        Text(text.uppercase(), style = EditorialEyebrow, color = Neutral600, maxLines = 1)
    }
}

// ---- Tabs --------------------------------------------------------------------------------------------------------

/** "Preguntar · Recientes" (P115, P118): a sand track with a white pill that slides to the chosen tab. */
@Composable
private fun Tabs(selected: AssistantTab, onSelect: (AssistantTab) -> Unit) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(Neutral100)
            .padding(4.dp),
    ) {
        val half = maxWidth / 2
        val still = rememberAnimationsOff()
        val offset by animateDpAsState(
            targetValue = if (selected == AssistantTab.ASK) 0.dp else half,
            animationSpec = if (still) tween(0) else spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMedium),
            label = "assistant-tab",
        )
        Box(
            Modifier
                .offset { IntOffset(offset.roundToPx(), 0) }
                .width(half)
                .fillMaxHeight()
                .shadow(2.dp, CircleShape, ambientColor = ShadowTint, spotColor = ShadowTint)
                .clip(CircleShape)
                .background(Neutral0),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            TabLabel(stringResource(R.string.assistant_tab_ask), null, selected == AssistantTab.ASK, Modifier.weight(1f)) {
                onSelect(AssistantTab.ASK)
            }
            TabLabel(stringResource(R.string.assistant_tab_recent), R.drawable.ic_history, selected == AssistantTab.RECENT, Modifier.weight(1f)) {
                onSelect(AssistantTab.RECENT)
            }
        }
    }
}

@Composable
private fun TabLabel(text: String, icon: Int?, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val color = if (selected) Neutral900 else Neutral600
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleSmall, color = color)
    }
}

// ---- Questions ---------------------------------------------------------------------------------------------------

@Composable
private fun QuestionsLoading() {
    val pulse = rememberPulse()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) { QuestionRowSkeleton(alpha = pulse) }
    }
}

/** The questions of the screen (P115, P121); offline they stay listed, disabled, under the notice (P126). */
@Composable
private fun QuestionsPage(state: AssistantSheetUiState, actions: AssistantSheetActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.offline) {
            AssistantNotice(stringResource(R.string.assistant_offline_notice), icon = R.drawable.ic_cloud_sync)
        }
        when (val catalog = state.catalog) {
            Catalog.Loading -> QuestionsLoading()
            is Catalog.Failed -> {
                val message = if (catalog.reason == AskFailure.NoPlan) {
                    stringResource(R.string.assistant_error_no_plan)
                } else {
                    stringResource(R.string.assistant_questions_error)
                }
                AssistantNotice(message)
                if (catalog.reason != AskFailure.NoPlan && !state.offline) {
                    SheetPill(stringResource(R.string.assistant_retry), actions.onReloadQuestions, filled = true, modifier = Modifier.fillMaxWidth())
                }
            }
            is Catalog.Ready -> catalog.questions.forEach { question ->
                QuestionRow(question = question, online = !state.offline, onClick = { actions.onAsk(question) })
            }
        }
        Footer()
    }
}

/** A question (P115): its icon on a pale yellow circle, the label and a chevron; without data it is grey with the reason. */
@Composable
private fun QuestionRow(question: AssistantQuestion, online: Boolean, onClick: () -> Unit) {
    val enabled = question.enabled && online
    val reason = question.disabledReason?.takeIf { !question.enabled }
    val description = if (reason != null) stringResource(R.string.assistant_question_disabled_a11y, question.label, reason) else question.label
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (enabled) Neutral0 else Neutral0.copy(alpha = 0.55f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(if (enabled) Harvest100 else Neutral100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(question.icon.drawable()),
                contentDescription = null,
                tint = if (enabled) Neutral900 else Neutral500,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                question.label,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
                color = if (enabled) Neutral900 else Neutral500,
            )
            if (reason != null) {
                Text(reason, style = MaterialTheme.typography.bodySmall, color = Neutral500)
            }
        }
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = if (enabled) Neutral900 else Neutral500.copy(alpha = 0.6f),
            modifier = Modifier.size(22.dp),
        )
    }
}

/** "● Viora solo lee tus datos; no cambia nada en tu lote." */
@Composable
private fun Footer() {
    Row(
        Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(Green800))
        Text(stringResource(R.string.assistant_footer), style = MaterialTheme.typography.bodySmall, color = Neutral600)
    }
}

// ---- A question: thinking, answer, failure ---------------------------------------------------------------------

/**
 * The page of a question: the chip of what was asked stays while what is under it changes, from
 * the brote thinking (P116) to the answer (P117) or the failure (P127).
 */
@Composable
private fun QuestionPage(state: AssistantSheetUiState, actions: AssistantSheetActions) {
    val page = state.page
    val question = when (page) {
        is AskPage.Thinking -> page.question
        is AskPage.Answered -> page.question
        is AskPage.Failed -> page.question
        else -> return
    }
    val stale = (page as? AskPage.Answered)?.answer?.isStale == true
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (stale) AssistantNotice(stringResource(R.string.assistant_stale_notice))
        QuestionChip(question, Modifier.alpha(if (stale) 0.55f else 1f))
        val bodyKey = when (page) {
            is AskPage.Thinking -> "thinking"
            is AskPage.Answered -> "answer-${page.answer.id}"
            else -> "failed"
        }
        AnimatedContent(
            targetState = bodyKey,
            transitionSpec = {
                fadeIn(tween(260, delayMillis = 80)).togetherWith(fadeOut(tween(120)))
                    .using(SizeTransform(clip = false) { _, _ -> spring(stiffness = Spring.StiffnessMediumLow) })
            },
            label = "assistant-question-body",
        ) { key ->
            when {
                key == "thinking" -> ThinkingBody(caption = thinkingCaption(state.screen))
                key.startsWith("answer-") -> (state.page as? AskPage.Answered)?.let { answered ->
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        AnswerBody(answered.answer)
                        if (answered.answer.isStale) {
                            AskAgainButton(refreshing = answered.refreshing, onClick = actions.onAskAgain)
                        } else {
                            ButtonPair(
                                primary = stringResource(R.string.assistant_another_question),
                                onPrimary = actions.onAnotherQuestion,
                                secondary = stringResource(R.string.assistant_recent_button),
                                onSecondary = { actions.onTab(AssistantTab.RECENT) },
                                secondaryIcon = R.drawable.ic_history,
                            )
                        }
                    }
                }
                else -> (state.page as? AskPage.Failed)?.let { failed -> FailedPage(failed.reason, actions) }
            }
        }
    }
}

@Composable
private fun FailedPage(reason: AskFailure, actions: AssistantSheetActions) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        FailedBody(reason)
        val retryable = reason == AskFailure.Generic || reason == AskFailure.Offline
        if (retryable) {
            ButtonPair(
                primary = stringResource(R.string.assistant_retry),
                onPrimary = actions.onRetry,
                secondary = stringResource(R.string.assistant_another_question),
                onSecondary = actions.onAnotherQuestion,
                primaryIcon = R.drawable.ic_cloud_sync,
            )
        } else {
            ButtonPair(
                primary = stringResource(R.string.assistant_another_question),
                onPrimary = actions.onAnotherQuestion,
                secondary = stringResource(R.string.assistant_recent_button),
                onSecondary = { actions.onTab(AssistantTab.RECENT) },
                secondaryIcon = R.drawable.ic_history,
            )
        }
    }
}

@Composable
private fun thinkingCaption(screen: AssistantScreen): String = when (screen) {
    AssistantScreen.ALTERNATION -> stringResource(R.string.assistant_thinking_alternation)
    AssistantScreen.PLAN -> stringResource(R.string.assistant_thinking_plan)
}

/** A slow breathing for placeholders; still when the phone has the animations off. */
@Composable
internal fun rememberPulse(): Float {
    if (rememberAnimationsOff()) return 1f
    val transition = rememberInfiniteTransition(label = "assistant-pulse")
    val value by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "assistant-pulse-alpha",
    )
    return value
}
