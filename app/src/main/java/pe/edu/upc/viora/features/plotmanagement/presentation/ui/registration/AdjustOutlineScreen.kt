package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline
import pe.edu.upc.viora.features.plotmanagement.presentation.state.AdjustOutlineUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.AdjustOutlineViewModel

/**
 * Moves the corners of a registered plot's outline on the satellite map (Figma P29): each corner is
 * dragged where it belongs, the area is recalculated live next to the one the plot had, and a corner
 * can be added where the crosshair is. Nothing is saved until "Guardar contorno".
 */
@Composable
fun AdjustOutlineScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdjustOutlineViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    when (state) {
        AdjustOutlineUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        AdjustOutlineUiState.NotFound -> Box(modifier.fillMaxSize().statusBarsPadding().padding(24.dp)) {
            CircleIconButton(
                icon = R.drawable.ic_arrow_back,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack,
            )
            Text(
                text = stringResource(R.string.plot_detail_not_found_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        is AdjustOutlineUiState.Adjusting -> {
            // Saved: the detail behind this screen already shows the new outline (it observes the cache).
            LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }
            AdjustOutlineContent(
                state = state,
                onMoveCorner = viewModel::moveCorner,
                onAddCorner = viewModel::addCorner,
                onUndo = viewModel::undo,
                onSave = viewModel::save,
                onBack = onBack,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun AdjustOutlineContent(
    state: AdjustOutlineUiState.Adjusting,
    onMoveCorner: (id: Int, point: GeoPoint) -> Unit,
    onAddCorner: (GeoPoint) -> Unit,
    onUndo: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewportState = rememberMapViewportState()
    var sheetExpanded by rememberSaveable { mutableStateOf(true) }
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    Box(modifier = modifier.fillMaxSize()) {
        PlotTraceMap(
            corners = state.points,
            initialCenter = state.outlineCenter,
            viewportState = viewportState,
            onCornerTapped = {},
            bottomInsetPx = sheetHeightPx,
            // The corners are always draggable here; the map can still be panned to reach another place.
            moving = true,
            panWhileMoving = true,
            outline = state.corners,
            onCornerMoved = onMoveCorner,
            modifier = Modifier.fillMaxSize(),
        )
        // The camera is padded by the sheet height, so the crosshair sits in the middle of the visible map.
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = with(density) { sheetHeightPx.toDp() }),
            contentAlignment = Alignment.Center,
        ) {
            Crosshair()
        }

        Column(
            modifier = Modifier.statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Closing and going back both leave without saving: nothing has been sent yet.
            StepHeader(step = 2, title = state.plot.name, onBack = onBack, onClose = onBack)
            MapHintChip(text = stringResource(R.string.adjust_hint))
        }

        AdjustSheet(
            state = state,
            expanded = sheetExpanded,
            onExpandedChange = { sheetExpanded = it },
            onAddCornerAtCenter = {
                // The map camera is the source of truth for "where the crosshair is".
                val center = viewportState.cameraState?.center
                if (center != null) onAddCorner(GeoPoint(center.latitude(), center.longitude()))
            },
            onUndo = onUndo,
            onSave = onSave,
            modifier = Modifier.align(Alignment.BottomCenter).onSizeChanged { sheetHeightPx = it.height },
        )
    }
}

@Composable
private fun AdjustSheet(
    state: AdjustOutlineUiState.Adjusting,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddCornerAtCenter: () -> Unit,
    onUndo: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Text(text = stringResource(R.string.adjust_title), style = MaterialTheme.typography.headlineMedium)
            AdjustTiles(state = state, modifier = Modifier.padding(top = 12.dp))
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(
                icon = R.drawable.ic_undo,
                contentDescription = stringResource(R.string.trace_undo),
                onClick = { if (state.canUndo) onUndo() },
                size = 56,
            )
            ActionPill(
                icon = R.drawable.ic_location_on,
                text = stringResource(R.string.adjust_add_corner),
                onClick = onAddCornerAtCenter,
                modifier = Modifier.weight(1f),
            )
        }
        if (expanded) {
            PrimaryPillButton(
                text = stringResource(R.string.adjust_save),
                onClick = onSave,
                enabled = state.canSave,
                isLoading = state.isSaving,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        AdjustMessage(state = state, modifier = Modifier.padding(top = 12.dp))
    }
}

/** How many corners there are (and how many were moved) and the new area next to the one it had. */
@Composable
private fun AdjustTiles(state: AdjustOutlineUiState.Adjusting, modifier: Modifier = Modifier) {
    Row(modifier = modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatTile(
            value = state.corners.size.toString(),
            caption = if (state.movedCount > 0) {
                pluralStringResource(R.plurals.adjust_corners_moved, state.movedCount, state.movedCount)
            } else {
                stringResource(R.string.adjust_corners_label)
            },
            background = Neutral0,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        StatTile(
            value = stringResource(R.string.trace_area_value, formatHectares(state.areaHectares)),
            caption = stringResource(R.string.adjust_area_before, formatHectares(state.plot.areaHectares)),
            background = Harvest100,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

/** Why a corner was refused, or why the outline could not be saved. */
@Composable
private fun AdjustMessage(state: AdjustOutlineUiState.Adjusting, modifier: Modifier = Modifier) {
    val message: String? = when {
        state.outlineError == PlotOutline.Error.SelfIntersecting -> stringResource(R.string.trace_error_crossing)
        state.refusedMove -> stringResource(R.string.trace_error_move_crossing)
        else -> when (val failure = state.failure) {
            null, EditFailure.NameTaken -> null
            EditFailure.Outdated -> stringResource(R.string.adjust_error_outdated)
            is EditFailure.Rejected -> stringResource(R.string.save_error_rejected, failure.detail.orEmpty())
            is EditFailure.Other -> stringResource(failure.error.messageRes())
        }
    }
    if (message != null) {
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = Terracotta700, modifier = modifier)
    }
}
