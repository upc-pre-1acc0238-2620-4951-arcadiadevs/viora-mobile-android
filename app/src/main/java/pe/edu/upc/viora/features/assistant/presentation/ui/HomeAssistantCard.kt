package pe.edu.upc.viora.features.assistant.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.assistant.domain.entity.AnswerFact
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.domain.entity.DailySummary
import pe.edu.upc.viora.features.assistant.domain.entity.FactTone
import pe.edu.upc.viora.features.assistant.presentation.state.DailySummaryCardUiState
import pe.edu.upc.viora.features.assistant.presentation.viewmodel.DailySummaryViewModel

/** What the card has open on top of Inicio. */
private enum class OpenSheet { NONE, QUESTIONS, SUMMARY }

/**
 * The Viora card of Inicio (Figma P110, P111): the summary of the day in one line, opened with the eye into its
 * voice and three cards, "Pregúntale a Viora" (P112) and "Ver completo" (P113). [plotId] is the plot Inicio is
 * focused on, which the "LOTE" chips of the sheet start on; the summary is always of the whole field.
 */
@Composable
fun HomeAssistantCard(
    plotId: String?,
    plotName: String,
    modifier: Modifier = Modifier,
    viewModel: DailySummaryViewModel = hiltViewModel(),
) {
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    if (state == DailySummaryCardUiState.Hidden) return
    var expanded by rememberSaveable { mutableStateOf(false) }
    var open by rememberSaveable { mutableStateOf(OpenSheet.NONE) }
    val summary = (state as? DailySummaryCardUiState.Ready)?.summary

    HomeAssistantCardContent(
        state = state,
        expanded = expanded && summary != null,
        onToggle = { expanded = !expanded },
        onAsk = { open = OpenSheet.QUESTIONS },
        onSeeFull = { open = OpenSheet.SUMMARY },
        modifier = modifier,
    )

    if (open != OpenSheet.NONE) {
        val question = stringResource(R.string.assistant_summary_question)
        AssistantSheet(
            screen = AssistantScreen.HOME,
            plotId = plotId,
            plotName = plotName,
            onDismiss = { open = OpenSheet.NONE },
            opening = summary?.takeIf { open == OpenSheet.SUMMARY }?.asAnswer(question),
        )
    }
}

/** The card, stateless: its collapsed line or its open summary, and the ask button below. */
@Composable
internal fun HomeAssistantCardContent(
    state: DailySummaryCardUiState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAsk: () -> Unit,
    onSeeFull: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = (state as? DailySummaryCardUiState.Ready)?.summary
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Neutral0)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AnimatedContent(
            targetState = expanded,
            transitionSpec = {
                fadeIn(tween(220, delayMillis = 60)).togetherWith(fadeOut(tween(120)))
                    .using(SizeTransform(clip = false) { _, _ -> spring(stiffness = Spring.StiffnessMediumLow) })
            },
            label = "home-assistant-card",
        ) { open ->
            if (open && summary != null) {
                OpenSummary(summary = summary, onHide = onToggle)
            } else {
                CollapsedLine(state = state, onShow = onToggle)
            }
        }
        PrimaryPillButton(
            text = stringResource(R.string.assistant_entry),
            onClick = onAsk,
            trailingIcon = R.drawable.ic_chevron_right,
        )
        if (expanded && summary != null) {
            SheetPill(
                text = stringResource(R.string.assistant_summary_full),
                onClick = onSeeFull,
                filled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** P110: "Resumen de hoy" and its three figures in one line, with the eye that opens it. */
@Composable
private fun CollapsedLine(state: DailySummaryCardUiState, onShow: () -> Unit) {
    val summary = (state as? DailySummaryCardUiState.Ready)?.summary
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CalendarBadge()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.assistant_summary_title), style = MaterialTheme.typography.titleSmall, color = Neutral900)
            Text(
                text = when {
                    summary != null -> summaryLine(summary)
                    state == DailySummaryCardUiState.Loading -> stringResource(R.string.assistant_summary_loading)
                    else -> stringResource(R.string.assistant_summary_missing)
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (summary != null) {
            CircleIconButton(
                icon = R.drawable.ic_visibility,
                contentDescription = stringResource(R.string.assistant_summary_show),
                onClick = onShow,
                background = Neutral100,
            )
        }
    }
}

/** P111: when it was written, Viora's voice and the three cards of the same height. */
@Composable
private fun OpenSummary(summary: DailySummary, onHide: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CalendarBadge()
            Spacer(Modifier.width(12.dp))
            SummaryOverline(summary.createdAt, Modifier.weight(1f))
            CircleIconButton(
                icon = R.drawable.ic_close,
                contentDescription = stringResource(R.string.assistant_summary_hide),
                onClick = onHide,
                background = Neutral100,
            )
        }
        VioraVoice(text = summary.text)
        SummaryTiles(facts = summary.facts)
    }
}

@Composable
private fun CalendarBadge() {
    Box(Modifier.size(40.dp).clip(CircleShape).background(Harvest100), contentAlignment = Alignment.Center) {
        Icon(painterResource(R.drawable.ic_calendar_month), contentDescription = null, tint = Green800, modifier = Modifier.size(20.dp))
    }
}

/** "12 días de ventana · 34° máx. jueves · 2 alertas activas": the three figures with what each one is. */
private fun summaryLine(summary: DailySummary): String =
    summary.facts.joinToString(" · ") { "${it.value} ${it.label}" }

private val previewSummary = DailySummary(
    id = "preview",
    createdAt = java.time.Instant.parse("2026-11-18T12:02:00Z"),
    text = "Hoy toca aclarear La Yarada 02: te quedan 12 días de ventana. El jueves se espera golpe de calor; riega temprano.",
    facts = listOf(
        AnswerFact("12", "días de ventana", "", FactTone.PRIMARY),
        AnswerFact("34°", "máx. jueves", "", FactTone.PRIMARY),
        AnswerFact("2", "alertas activas", "", FactTone.PRIMARY),
    ),
    sources = emptyList(),
)

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, widthDp = 402)
@Composable
private fun HomeAssistantCardCollapsedPreview() {
    VioraTheme {
        HomeAssistantCardContent(
            state = DailySummaryCardUiState.Ready(previewSummary),
            expanded = false,
            onToggle = {},
            onAsk = {},
            onSeeFull = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, widthDp = 402)
@Composable
private fun HomeAssistantCardOpenPreview() {
    VioraTheme {
        HomeAssistantCardContent(
            state = DailySummaryCardUiState.Ready(previewSummary),
            expanded = true,
            onToggle = {},
            onAsk = {},
            onSeeFull = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

