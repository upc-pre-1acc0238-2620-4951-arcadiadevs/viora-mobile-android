package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares

/**
 * Step 1: the producer moves the satellite map under a fixed crosshair and adds a corner at the
 * centre, or taps the map directly. Undo removes the last corner; closing validates the outline.
 * The bottom sheet can be folded (drag the handle down) to leave the whole map free for tracing.
 */
@Composable
fun TraceStep(
    state: RegisterPlotUiState,
    onAddCorner: (GeoPoint) -> Unit,
    onMoveCorner: (id: Int, point: GeoPoint) -> Unit,
    onUndo: () -> Unit,
    onCloseOutline: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewportState = rememberMapViewportState()
    var moving by rememberSaveable { mutableStateOf(false) }
    var sheetExpanded by rememberSaveable { mutableStateOf(true) }
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    Box(modifier = modifier.fillMaxSize()) {
        PlotTraceMap(
            corners = state.corners,
            // With corners already marked (e.g. walked with the GPS), open where they are.
            initialCenter = state.outlineCenter ?: state.mapCenter,
            viewportState = viewportState,
            onCornerTapped = onAddCorner,
            bottomInsetPx = sheetHeightPx,
            moving = moving,
            outline = state.outline,
            onCornerMoved = onMoveCorner,
            modifier = Modifier.fillMaxSize(),
        )
        // The map camera is padded by the sheet height, so its centre is the middle of the part of
        // the map the producer can see: the crosshair is drawn there.
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = with(density) { sheetHeightPx.toDp() }),
            contentAlignment = Alignment.Center,
        ) {
            if (!moving) Crosshair()
        }

        Column(
            modifier = Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepHeader(step = RegisterPlotStep.TRACE.number, onBack = onBack, onClose = onClose)
            MapHintChip()
        }

        TraceSheet(
            state = state,
            expanded = sheetExpanded,
            onExpandedChange = { sheetExpanded = it },
            moving = moving,
            onMovingChange = { moving = it },
            onAddCornerAtCenter = {
                // The map camera is the source of truth for "where the crosshair is".
                val center = viewportState.cameraState?.center
                if (center != null) onAddCorner(GeoPoint(center.latitude(), center.longitude()))
            },
            onUndo = onUndo,
            onCloseOutline = onCloseOutline,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { sheetHeightPx = it.height },
        )
    }
}

@Composable
internal fun MapHintChip(text: String = stringResource(R.string.trace_map_hint)) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Neutral0)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(Harvest300))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = Neutral900)
    }
}

/** A fixed aiming mark in the middle of the visible map; drawn, since it is a map overlay, not an asset. */
@Composable
internal fun Crosshair(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(52.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val ring = 14.dp.toPx()
        val reach = 24.dp.toPx()
        val gap = 8.dp.toPx()
        val stroke = Stroke(width = 1.5.dp.toPx())
        drawCircle(color = Neutral900, radius = ring, center = center, style = stroke)
        listOf(Offset(0f, -1f), Offset(0f, 1f), Offset(-1f, 0f), Offset(1f, 0f)).forEach { dir ->
            drawLine(
                color = Neutral900,
                start = center + dir * gap,
                end = center + dir * reach,
                strokeWidth = 1.5.dp.toPx(),
            )
        }
        drawCircle(color = Terracotta500, radius = 3.dp.toPx(), center = center)
    }
}

private const val DRAG_THRESHOLD_PX = 24f

/** The handle: dragging it down folds the sheet, dragging it up (or tapping it) opens it. */
@Composable
internal fun SheetHandle(expanded: Boolean, onExpandedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var dragged by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(expanded) {
                detectVerticalDragGestures(
                    onDragStart = { dragged = 0f },
                    onDragEnd = {
                        if (dragged > DRAG_THRESHOLD_PX) onExpandedChange(false)
                        if (dragged < -DRAG_THRESHOLD_PX) onExpandedChange(true)
                    },
                    onVerticalDrag = { _, amount -> dragged += amount },
                )
            }
            .clickable(role = Role.Button) { onExpandedChange(!expanded) },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(Neutral900.copy(alpha = 0.2f)),
        )
    }
}

@Composable
private fun TraceSheet(
    state: RegisterPlotUiState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    moving: Boolean,
    onMovingChange: (Boolean) -> Unit,
    onAddCornerAtCenter: () -> Unit,
    onUndo: () -> Unit,
    onCloseOutline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val crossing = state.outlineError == PlotOutline.Error.SelfIntersecting
    val cornerCount = state.corners.size
    val leadTitle = when {
        moving -> R.string.trace_move_title_lead
        crossing -> R.string.trace_title_cross_lead
        else -> R.string.trace_title_lead
    }
    val emphasisTitle = when {
        moving -> R.string.trace_move_title_emphasis
        crossing -> R.string.trace_title_cross_emphasis
        else -> R.string.trace_title_emphasis
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .navigationBarsPadding()
            .animateContentSize()
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
    ) {
        SheetHandle(expanded = expanded, onExpandedChange = onExpandedChange)
        if (expanded) {
            val titleStyle = MaterialTheme.typography.headlineMedium
            Text(text = stringResource(leadTitle), style = titleStyle)
            Text(
                text = stringResource(emphasisTitle),
                style = titleStyle,
                fontStyle = FontStyle.Italic,
            )
            CornerStatTiles(
                cornerCount = cornerCount,
                areaHectares = state.areaHectares,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (moving) {
                ActionPill(
                    icon = R.drawable.ic_check,
                    text = stringResource(R.string.trace_done),
                    onClick = { onMovingChange(false) },
                    modifier = Modifier.weight(1f),
                )
            } else {
                CircleIconButton(
                    icon = R.drawable.ic_undo,
                    contentDescription = stringResource(R.string.trace_undo),
                    onClick = onUndo,
                    size = 56,
                )
                CircleIconButton(
                    icon = R.drawable.ic_move,
                    contentDescription = stringResource(R.string.trace_move),
                    onClick = { onMovingChange(true) },
                    size = 56,
                )
                ActionPill(
                    icon = R.drawable.ic_location_on,
                    text = stringResource(R.string.trace_add_corner, cornerCount + 1),
                    onClick = onAddCornerAtCenter,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (expanded && !moving) {
            PrimaryPillButton(
                text = stringResource(R.string.trace_close_outline),
                onClick = onCloseOutline,
                enabled = cornerCount >= PlotOutline.MIN_CORNERS,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        // A refused corner is explained even when the sheet is folded; the "how many" hint is not worth the space then.
        if (crossing || state.refusedMove || expanded) {
            TraceMessage(
                cornerCount = cornerCount,
                crossing = crossing,
                refusedMove = state.refusedMove,
                moving = moving,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

/** The two figures of the outline sheets: how many corners there are and the provisional area. */
@Composable
internal fun CornerStatTiles(cornerCount: Int, areaHectares: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(
            value = cornerCount.toString(),
            caption = stringResource(R.string.trace_corners_label),
            background = Neutral0,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            value = if (cornerCount >= PlotOutline.MIN_CORNERS) stringResource(R.string.trace_area_value, formatHectares(areaHectares)) else "—",
            caption = stringResource(R.string.trace_area_label),
            background = Harvest100,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

/**
 * The yellow pill of the action row: the main thing to do next (add a corner, or finish moving).
 * Disabled it turns grey: the action is not possible right now.
 */
@Composable
internal fun ActionPill(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .background(if (enabled) Harvest300 else Neutral200)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = if (enabled) Neutral900 else Neutral600)
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) Neutral900 else Neutral600,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/** One line under the actions: why something was refused, or the hint that fits the moment. */
@Composable
private fun TraceMessage(
    cornerCount: Int,
    crossing: Boolean,
    refusedMove: Boolean,
    moving: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        crossing -> Text(
            text = stringResource(R.string.trace_error_crossing),
            style = MaterialTheme.typography.bodySmall,
            color = Terracotta700,
            modifier = modifier,
        )
        refusedMove -> Text(
            text = stringResource(R.string.trace_error_move_crossing),
            style = MaterialTheme.typography.bodySmall,
            color = Terracotta700,
            modifier = modifier,
        )
        moving -> Text(
            text = stringResource(R.string.trace_move_hint),
            style = MaterialTheme.typography.bodySmall,
            color = Neutral600,
            modifier = modifier,
        )
        cornerCount < PlotOutline.MIN_CORNERS -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = null,
                tint = Neutral600,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.trace_hint_min_corners, cornerCount),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
    }
}
