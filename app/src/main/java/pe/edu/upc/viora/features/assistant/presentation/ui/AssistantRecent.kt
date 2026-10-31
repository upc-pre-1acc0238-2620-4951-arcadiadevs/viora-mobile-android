package pe.edu.upc.viora.features.assistant.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.theme.EditorialEyebrow
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.newsreader
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.core.presentation.formatClock
import pe.edu.upc.viora.core.presentation.formatDayMonth
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantPresenter
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantTab
import pe.edu.upc.viora.features.assistant.presentation.state.RecentAnswer
import java.time.Instant

/**
 * "Recientes" (P118, P128): the last 10 answers of the last 10 days grouped by day, each with its
 * question, the start of the answer, when and where, and whether its data is still current.
 */
@Composable
internal fun RecentPage(recent: List<RecentAnswer>?, now: Instant, actions: AssistantSheetActions) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            recent == null -> {
                val pulse = rememberPulse()
                repeat(2) { RecentSkeleton(pulse) }
            }
            recent.isEmpty() -> EmptyRecent(onAsk = { actions.onTab(AssistantTab.ASK) })
            else -> {
                Text(stringResource(R.string.assistant_recent_caption), style = MaterialTheme.typography.bodySmall, color = Neutral600)
                AssistantPresenter.groupByDay(recent, now, LocalAppZone.current).forEach { day ->
                    Text(
                        text = when (day.daysAgo) {
                            0 -> stringResource(R.string.assistant_recent_today)
                            1 -> stringResource(R.string.assistant_recent_yesterday)
                            else -> stringResource(R.string.assistant_recent_days_ago, day.daysAgo)
                        }.uppercase(),
                        style = EditorialEyebrow,
                        color = Neutral600,
                        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                    )
                    day.answers.forEach { item ->
                        RecentCard(item = item, today = day.daysAgo == 0, onClick = { actions.onOpenRecent(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentCard(item: RecentAnswer, today: Boolean, onClick: () -> Unit) {
    val answer = item.answer
    val zone = LocalAppZone.current
    val whenText = if (today) formatClock(answer.createdAt, zone) else formatDayMonth(answer.createdAt.atZone(zone).toLocalDate())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = answer.question ?: stringResource(R.string.assistant_question_fallback),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = Neutral900,
                modifier = Modifier.weight(1f),
            )
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Neutral900, modifier = Modifier.size(22.dp))
        }
        Text(
            text = answer.text,
            style = newsreader(16.sp, 22.sp).copy(fontStyle = FontStyle.Italic),
            color = Neutral700,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = item.plotName?.let { stringResource(R.string.assistant_recent_meta, whenText, it) } ?: whenText,
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
            StatusChip(stale = answer.isStale)
        }
    }
}

/** "Al día" in sage, "Datos nuevos" in yellow (P118). */
@Composable
private fun StatusChip(stale: Boolean) {
    Text(
        text = stringResource(if (stale) R.string.assistant_recent_stale else R.string.assistant_recent_fresh),
        style = MaterialTheme.typography.labelSmall,
        color = if (stale) Harvest800 else Green800,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (stale) Harvest100 else Green100)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

/** P128: no answers yet, with the way back to the questions. */
@Composable
private fun EmptyRecent(onAsk: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Neutral0)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(Harvest100), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_history), contentDescription = null, tint = Neutral900, modifier = Modifier.size(26.dp))
            }
            Text(
                stringResource(R.string.assistant_recent_empty_title),
                style = newsreader(24.sp, 30.sp),
                color = Neutral900,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.assistant_recent_empty_body),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                textAlign = TextAlign.Center,
            )
        }
        PrimaryPillButton(text = stringResource(R.string.assistant_recent_empty_action), onClick = onAsk)
    }
}

@Composable
private fun RecentSkeleton(alpha: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .padding(18.dp)
            .alpha(alpha),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.fillMaxWidth(0.6f).height(14.dp).clip(CircleShape).background(Neutral100))
        Box(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(Neutral100))
        Box(Modifier.fillMaxWidth(0.4f).height(12.dp).clip(CircleShape).background(Neutral100))
    }
}
