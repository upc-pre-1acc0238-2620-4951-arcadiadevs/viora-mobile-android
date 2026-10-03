package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsSignal
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.state.GpsReading
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotUiState

/**
 * Step 2 with the GPS: the producer walks to each corner of the plot and marks it with the
 * position of the phone. A corner can only be marked while the signal is good enough
 * ([GpsSignal.allowsMarking]); when the GPS is lost the producer can carry on with the map, keeping
 * the corners already marked, or wait for the signal to come back.
 */
@Composable
fun GpsStep(
    state: RegisterPlotUiState,
    reading: GpsReading,
    onMarkCorner: (GeoPoint) -> Unit,
    onUndo: () -> Unit,
    onCloseOutline: () -> Unit,
    onUseMap: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheetExpanded by rememberSaveable { mutableStateOf(true) }
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    // "Wait for the signal" hides the lost-signal sheet until the GPS comes back.
    var waiting by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(reading is GpsReading.Located) {
        if (reading is GpsReading.Located) waiting = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        PlotGpsMap(
            outline = state.outline,
            reading = reading,
            initialCenter = state.mapCenter,
            bottomInsetPx = sheetHeightPx,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StepHeader(step = RegisterPlotStep.TRACE.number, onBack = onBack, onClose = onClose)
            GpsStatusPill(reading)
        }
        val sheetModifier = Modifier
            .align(Alignment.BottomCenter)
            .onSizeChanged { sheetHeightPx = it.height }
        if (reading is GpsReading.Lost && !waiting) {
            LostSignalSheet(
                cornerCount = state.corners.size,
                expanded = sheetExpanded,
                onExpandedChange = { sheetExpanded = it },
                onUseMap = onUseMap,
                onWait = { waiting = true },
                modifier = sheetModifier,
            )
        } else {
            MarkingSheet(
                state = state,
                reading = reading,
                expanded = sheetExpanded,
                onExpandedChange = { sheetExpanded = it },
                onMarkCorner = onMarkCorner,
                onUndo = onUndo,
                onCloseOutline = onCloseOutline,
                modifier = sheetModifier,
            )
        }
    }
}

/** The chip under the header that says how good the GPS is right now. */
@Composable
private fun GpsStatusPill(reading: GpsReading) {
    val style = when (reading) {
        GpsReading.Searching -> PillStyle(stringResource(R.string.gps_status_searching), Neutral600, Neutral0, Neutral900)
        is GpsReading.Located -> {
            val meters = reading.fix.accuracyMeters.roundToInt().toString()
            when (reading.fix.signal) {
                GpsSignal.GOOD -> PillStyle(stringResource(R.string.gps_status_good, meters), Green800, Neutral0, Neutral900)
                GpsSignal.FAIR -> PillStyle(stringResource(R.string.gps_status_fair, meters), Harvest400, Neutral0, Neutral900)
                else -> PillStyle(stringResource(R.string.gps_status_weak, meters), Terracotta500, Terracotta100, Terracotta700)
            }
        }
        is GpsReading.Lost -> PillStyle(
            // The accuracy is only worth showing when it is the reason: fixes that stopped coming have none.
            text = reading.lastFix?.takeIf { it.signal == GpsSignal.LOST }
                ?.let { stringResource(R.string.gps_status_lost, it.accuracyMeters.roundToInt().toString()) }
                ?: stringResource(R.string.gps_status_lost_unknown),
            dot = Terracotta500,
            background = Terracotta100,
            content = Terracotta700,
        )
    }
    Row(
        modifier = Modifier.clip(CircleShape).background(style.background).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(style.dot))
        Text(text = style.text, style = MaterialTheme.typography.labelMedium, color = style.content)
    }
}

private data class PillStyle(
    val text: String,
    val dot: androidx.compose.ui.graphics.Color,
    val background: androidx.compose.ui.graphics.Color,
    val content: androidx.compose.ui.graphics.Color,
)

/** The sheet of the walking: the figures, "mark corner N" and "close the outline". */
@Composable
private fun MarkingSheet(
    state: RegisterPlotUiState,
    reading: GpsReading,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMarkCorner: (GeoPoint) -> Unit,
    onUndo: () -> Unit,
    onCloseOutline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cornerCount = state.corners.size
    val crossing = state.outlineError == PlotOutline.Error.SelfIntersecting
    val markableFix = (reading as? GpsReading.Located)?.fix?.takeIf { it.signal.allowsMarking }
    val haptics = LocalHapticFeedback.current

    SheetFrame(modifier = modifier) {
        SheetHandle(expanded = expanded, onExpandedChange = onExpandedChange)
        if (expanded) {
            val titleStyle = MaterialTheme.typography.headlineMedium
            val first = cornerCount == 0
            Text(text = stringResource(if (first) R.string.gps_title_first_lead else R.string.gps_title_next_lead), style = titleStyle)
            Text(
                text = stringResource(if (first) R.string.gps_title_first_emphasis else R.string.gps_title_next_emphasis),
                style = titleStyle,
                fontStyle = FontStyle.Italic,
            )
            CornerStatTiles(cornerCount = cornerCount, areaHectares = state.areaHectares, modifier = Modifier.padding(top = 12.dp))
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(
                icon = R.drawable.ic_undo,
                contentDescription = stringResource(R.string.trace_undo),
                onClick = onUndo,
                size = 56,
            )
            ActionPill(
                icon = R.drawable.ic_location_on,
                text = stringResource(R.string.gps_mark_corner, cornerCount + 1),
                enabled = markableFix != null,
                onClick = {
                    markableFix?.let {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMarkCorner(it.point)
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
        if (expanded) {
            PrimaryPillButton(
                text = stringResource(R.string.trace_close_outline),
                onClick = onCloseOutline,
                enabled = cornerCount >= PlotOutline.MIN_CORNERS,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        // A reason is always shown (even folded): the button is grey for a reason the producer must know.
        GpsMessage(
            reading = reading,
            cornerCount = cornerCount,
            crossing = crossing,
            showMinimumHint = expanded,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

/** One line under the buttons: what is wrong, or the hint that fits the moment. */
@Composable
private fun GpsMessage(
    reading: GpsReading,
    cornerCount: Int,
    crossing: Boolean,
    showMinimumHint: Boolean,
    modifier: Modifier = Modifier,
) {
    val weak = (reading as? GpsReading.Located)?.fix?.takeIf { it.signal == GpsSignal.WEAK }
    when {
        crossing -> MessageText(stringResource(R.string.trace_error_crossing), Terracotta700, modifier)
        weak != null -> MessageText(
            stringResource(R.string.gps_hint_weak, weak.accuracyMeters.roundToInt().toString()),
            Terracotta700,
            modifier,
        )
        reading is GpsReading.Searching -> MessageText(stringResource(R.string.gps_hint_searching), Neutral600, modifier)
        showMinimumHint && cornerCount < PlotOutline.MIN_CORNERS -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(R.drawable.ic_info), contentDescription = null, tint = Neutral600, modifier = Modifier.size(18.dp))
            Text(
                text = stringResource(R.string.trace_hint_min_corners, cornerCount),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
    }
}

@Composable
private fun MessageText(text: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = color, modifier = modifier)
}

/** The sheet shown when the GPS is lost: carry on with the map, or wait. */
@Composable
private fun LostSignalSheet(
    cornerCount: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onUseMap: () -> Unit,
    onWait: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetFrame(modifier = modifier) {
        SheetHandle(expanded = expanded, onExpandedChange = onExpandedChange)
        if (expanded) {
            val titleStyle = MaterialTheme.typography.headlineMedium
            Text(text = stringResource(R.string.gps_lost_title_lead), style = titleStyle)
            Text(text = stringResource(R.string.gps_lost_title_emphasis), style = titleStyle, fontStyle = FontStyle.Italic)
            Text(
                text = if (cornerCount == 0) {
                    stringResource(R.string.gps_lost_body_empty)
                } else {
                    pluralStringResource(R.plurals.gps_lost_body_kept, cornerCount, cornerCount)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
                modifier = Modifier.padding(top = 8.dp),
            )
            StatTile(
                value = cornerCount.toString(),
                caption = stringResource(R.string.trace_corners_label),
                background = Neutral0,
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
            )
        }
        ActionPill(
            icon = R.drawable.ic_map,
            text = stringResource(R.string.gps_keep_map),
            onClick = onUseMap,
            modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(Neutral0)
                .clickable(role = Role.Button, onClick = onWait),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.gps_wait_signal), style = MaterialTheme.typography.titleMedium, color = Neutral900)
        }
    }
}

/** The rounded bottom sheet shared by the walking states; it animates when its content changes. */
@Composable
private fun SheetFrame(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .navigationBarsPadding()
            .animateContentSize()
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
    ) { content() }
}
