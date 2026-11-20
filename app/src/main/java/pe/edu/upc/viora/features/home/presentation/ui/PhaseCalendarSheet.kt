package pe.edu.upc.viora.features.home.presentation.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraBottomSheet
import pe.edu.upc.viora.core.designsystem.component.VioraDatePicker
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.home.presentation.state.HomePhase
import pe.edu.upc.viora.features.home.presentation.state.PhaseCalendarRow
import pe.edu.upc.viora.features.home.presentation.state.PhaseTiming
import pe.edu.upc.viora.features.phenology.application.published.PhaseDateSource

/**
 * The calendar of the plot in focus: the five phases of the olive year with the day each one starts, whether that day
 * is the usual one, one the producer corrected or one the plot's own records fix, and the way to correct it. The
 * phases around the one that moves follow it, so the producer only says when their season really starts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhaseCalendarSheet(
    plotName: String,
    rows: List<PhaseCalendarRow>,
    onAdjust: (HomePhase, LocalDate) -> Unit,
    onReset: (HomePhase) -> Unit,
    onDismiss: () -> Unit,
) {
    var adjusting by rememberSaveable { mutableStateOf<HomePhase?>(null) }
    val row = rows.firstOrNull { it.phase == adjusting }
    val bounds = row?.bounds

    VioraBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.phase_calendar_title, plotName), style = MaterialTheme.typography.headlineSmall, color = Neutral900)
            Text(
                stringResource(R.string.phase_calendar_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            rows.forEachIndexed { index, item ->
                if (index > 0) HorizontalDivider(color = Neutral200)
                PhaseCalendarRowView(item, onAdjust = { adjusting = item.phase }, onReset = { onReset(item.phase) })
            }
        }
    }

    if (row != null && bounds != null) {
        VioraDatePicker(
            selected = row.startsOn,
            min = bounds.earliest,
            max = bounds.latest,
            onPick = { on ->
                adjusting = null
                onAdjust(row.phase, on)
            },
            onDismiss = { adjusting = null },
        )
    }
}

@Composable
private fun PhaseCalendarRowView(row: PhaseCalendarRow, onAdjust: () -> Unit, onReset: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(row.phase.label), style = MaterialTheme.typography.titleMedium, color = Neutral900)
            SourceTag(row.source)
        }
        Text(
            stringResource(R.string.phase_calendar_range, shortDate(row.startsOn), shortDate(row.endsOn)),
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600,
        )
        if (row.timing == PhaseTiming.NOW) {
            Text(stringResource(R.string.phase_calendar_now), style = MaterialTheme.typography.labelMedium, color = Green900)
        }
        if (row.canAdjust || row.canReset) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (row.canAdjust) TextButton(onClick = onAdjust) { Text(stringResource(R.string.phase_calendar_adjust)) }
                if (row.canReset) TextButton(onClick = onReset) { Text(stringResource(R.string.phase_calendar_reset)) }
            }
        } else {
            Text(stringResource(R.string.phase_calendar_recorded_hint), style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
    }
}

/** The small tag that tells an estimate from a date of the producer: usual, adjusted by them or fixed by their plot. */
@Composable
private fun SourceTag(source: PhaseDateSource) {
    val background = when (source) {
        PhaseDateSource.USUAL -> Neutral200
        PhaseDateSource.ADJUSTED -> Harvest300
        PhaseDateSource.RECORDED -> Green200
    }
    Text(
        text = stringResource(source.label),
        style = MaterialTheme.typography.labelMedium,
        color = Neutral900,
        modifier = Modifier.clip(CircleShape).background(background).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@get:StringRes
private val PhaseDateSource.label: Int
    get() = when (this) {
        PhaseDateSource.USUAL -> R.string.phase_source_usual
        PhaseDateSource.ADJUSTED -> R.string.phase_source_adjusted
        PhaseDateSource.RECORDED -> R.string.phase_source_recorded
    }
