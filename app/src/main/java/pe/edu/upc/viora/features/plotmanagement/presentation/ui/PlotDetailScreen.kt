package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraSectionHeader
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.presentation.ui.formatIndex
import pe.edu.upc.viora.features.phenology.presentation.ui.sentenceRes
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.ArchiveState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.LotHarvest
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotDetailUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotDetailViewModel

/** Distance from the top of the screen to the sheet when it is open: the map shows above it. */
private val OpenSheetTop = 340.dp

/** What the lowered sheet still shows above the tab bar: the grabber, the name and the frame. */
private val LoweredSheetContent = 112.dp

/**
 * A plot's detail (Figma P26): its outline on the satellite map and a sheet with its status and
 * figures. The sheet has two positions. Lowering it (dragging its grabber, tapping the grabber,
 * or double-tapping the map) leaves the map almost full screen and unlocks it so the producer can
 * explore around the plot; raising it brings back the framed outline.
 */
@Composable
fun PlotDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAdjustOutline: () -> Unit,
    onSensors: () -> Unit = {},
    onHarvestHistory: (plotName: String) -> Unit = {},
    onWinterChill: (plotName: String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PlotDetailViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val archiveState = viewModel.archiveState.collectAsStateWithLifecycle().value
    // Archived: the plot is gone from the active list, so leave its detail.
    LaunchedEffect(archiveState) { if (archiveState == ArchiveState.Done) onBack() }
    PlotDetailContent(
        state = state,
        onBack = onBack,
        onEdit = onEdit,
        onAdjustOutline = onAdjustOutline,
        onSensors = onSensors,
        onHarvestHistory = onHarvestHistory,
        onWinterChill = onWinterChill,
        archiveState = archiveState,
        onArchive = viewModel::archive,
        onDismissArchiveFailure = viewModel::dismissArchiveFailure,
        modifier = modifier,
    )
}

@Composable
fun PlotDetailContent(
    state: PlotDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onAdjustOutline: () -> Unit = {},
    onSensors: () -> Unit = {},
    onHarvestHistory: (plotName: String) -> Unit = {},
    onWinterChill: (plotName: String) -> Unit = {},
    archiveState: ArchiveState = ArchiveState.Idle,
    onArchive: () -> Unit = {},
    onDismissArchiveFailure: () -> Unit = {},
) {
    when (state) {
        PlotDetailUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        PlotDetailUiState.NotFound -> PlotNotFound(onBack = onBack, modifier = modifier)
        is PlotDetailUiState.Content -> PlotDetailBody(
            plot = state.plot,
            showSavedNotice = state.showSavedNotice,
            onBack = onBack,
            onEdit = onEdit,
            onAdjustOutline = onAdjustOutline,
            onSensors = onSensors,
            onHarvestHistory = onHarvestHistory,
            onWinterChill = onWinterChill,
            activeHectares = state.activeHectares,
            harvest = state.harvest,
            archiveState = archiveState,
            onArchive = onArchive,
            onDismissArchiveFailure = onDismissArchiveFailure,
            modifier = modifier,
        )
    }
}

@Composable
private fun PlotDetailBody(
    plot: Plot,
    showSavedNotice: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAdjustOutline: () -> Unit,
    onSensors: () -> Unit,
    onHarvestHistory: (plotName: String) -> Unit,
    onWinterChill: (plotName: String) -> Unit = {},
    activeHectares: Double,
    harvest: LotHarvest?,
    archiveState: ArchiveState,
    onArchive: () -> Unit,
    onDismissArchiveFailure: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var optionsOpen by rememberSaveable { mutableStateOf(false) }
    var confirmingArchive by rememberSaveable { mutableStateOf(false) }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val openSheetHeight = maxHeight - OpenSheetTop
        // The lowered sheet leaves its content above the floating tab bar, which stays on screen.
        val loweredSheetHeight = LoweredSheetContent +
            VioraTabBarDefaults.ContentBottomPadding +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        var exploring by rememberSaveable { mutableStateOf(false) }
        BackHandler(enabled = exploring) { exploring = false }

        PlotDetailMap(
            corners = plot.outline,
            coveredHeight = if (exploring) loweredSheetHeight else openSheetHeight,
            interactive = exploring,
            modifier = Modifier.fillMaxSize(),
        )
        if (!exploring) {
            // While the map is a picture, two taps on it open it for exploring.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onDoubleTap = { exploring = true }) },
            )
        }
        TopBar(showSavedNotice = showSavedNotice, onBack = onBack, onMore = { optionsOpen = true })
        DetailSheet(
            plot = plot,
            exploring = exploring,
            onExploringChange = { exploring = it },
            sheetHeight = if (exploring) loweredSheetHeight else openSheetHeight,
            harvest = harvest,
            onHarvestHistory = { onHarvestHistory(plot.name) },
            onWinterChill = { onWinterChill(plot.name) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
        if (optionsOpen) {
            PlotOptionsSheet(
                plot = plot,
                onEdit = {
                    optionsOpen = false
                    onEdit()
                },
                onAdjustOutline = {
                    optionsOpen = false
                    onAdjustOutline()
                },
                onSensors = {
                    optionsOpen = false
                    onSensors()
                },
                onArchive = {
                    optionsOpen = false
                    confirmingArchive = true
                },
                onDismiss = { optionsOpen = false },
            )
        }
        if (confirmingArchive) {
            ArchivePlotDialog(
                plotName = plot.name,
                activeHectaresBefore = activeHectares,
                activeHectaresAfter = (activeHectares - plot.areaHectares).coerceAtLeast(0.0),
                state = archiveState,
                onConfirm = onArchive,
                onDismiss = {
                    confirmingArchive = false
                    onDismissArchiveFailure()
                },
            )
        }
    }
}

/** Back button on the left and, for a moment after registering, the "plot saved" notice centred. */
@Composable
private fun TopBar(showSavedNotice: Boolean, onBack: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        SavedNoticeSlot(visible = showSavedNotice, modifier = Modifier.weight(1f))
        CircleIconButton(
            icon = R.drawable.ic_more_vert,
            contentDescription = stringResource(R.string.plot_menu_more),
            onClick = onMore,
        )
    }
}

@Composable
private fun SavedNoticeSlot(visible: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            SavedNotice()
        }
    }
}

@Composable
private fun SavedNotice() {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Green900)
            .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Harvest300), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Neutral900,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = stringResource(R.string.plot_detail_saved),
            style = MaterialTheme.typography.labelLarge,
            color = Neutral50,
        )
    }
}

/**
 * The sheet. It changes height with the same soft animation as the registration wizard's sheet
 * ([animateContentSize]); when it is lowered only the grabber, the name and the frame remain.
 */
@Composable
private fun DetailSheet(
    plot: Plot,
    exploring: Boolean,
    onExploringChange: (Boolean) -> Unit,
    sheetHeight: Dp,
    harvest: LotHarvest?,
    onHarvestHistory: () -> Unit,
    onWinterChill: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(MaterialTheme.colorScheme.background)
            .animateContentSize()
            .height(sheetHeight)
            .padding(horizontal = 24.dp),
    ) {
        Grabber(exploring = exploring, onExploringChange = onExploringChange)
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = plot.name,
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, lineHeight = 40.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                StatusChip(isActive = plot.isActive)
            }
            Text(
                text = stringResource(
                    R.string.plot_detail_subtitle,
                    stringResource(plot.variety.labelRes()),
                    formatMeters(plot.rowSpacingMeters),
                    formatMeters(plot.treeSpacingMeters),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
            )
            if (!exploring) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlotStatTile(
                        value = stringResource(R.string.plot_detail_area_value, formatHectares(plot.areaHectares)),
                        caption = stringResource(R.string.plot_detail_area_caption),
                        modifier = Modifier.weight(1f),
                    )
                    PlotStatTile(
                        value = stringResource(R.string.plot_detail_density_value, formatCount(plot.treesPerHectare)),
                        caption = stringResource(R.string.plot_detail_density_caption),
                        modifier = Modifier.weight(1f),
                    )
                    PlotStatTile(
                        value = stringResource(R.string.review_trees_value, formatCount(plot.estimatedTrees)),
                        caption = stringResource(R.string.plot_detail_trees_caption),
                        modifier = Modifier.weight(1f),
                    )
                }
                VioraSectionHeader(title = stringResource(R.string.plot_detail_your_lot))
                LotSectionCard(
                    icon = R.drawable.ic_history,
                    title = stringResource(R.string.lot_section_harvest),
                    subtitle = harvestSubtitle(harvest),
                    background = Harvest100,
                    onClick = onHarvestHistory,
                )
                LotSectionCard(
                    icon = R.drawable.ic_ac_unit,
                    title = stringResource(R.string.winter_chill_title),
                    subtitle = stringResource(R.string.winter_chill_timeline),
                    background = Harvest100,
                    onClick = onWinterChill,
                )
            }
        }
    }
}

/** "Índice 0,51 · vecería severa", or how many campaigns are missing, or the generic line until it is known. */
@Composable
private fun harvestSubtitle(harvest: LotHarvest?): String = when {
    harvest == null -> stringResource(R.string.lot_section_harvest_hint)
    harvest.index != null -> stringResource(
        R.string.plot_detail_harvest_index,
        formatIndex(harvest.index),
        stringResource(BbiClass.of(harvest.index).sentenceRes()),
    )
    else -> pluralStringResource(R.plurals.plot_detail_harvest_missing, harvest.missingCampaigns, harvest.missingCampaigns)
}

/** A section of the plot as a card (Figma P26 "Tu lote"): icon, title and line, and an arrow to open it. */
@Composable
private fun LotSectionCard(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null, tint = Neutral900, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Neutral900)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Neutral700)
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(Green800), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral0, modifier = Modifier.size(20.dp))
        }
    }
}

private const val DRAG_THRESHOLD_PX = 24f

/**
 * The only part of the sheet that moves it, as in the registration wizard: dragging the grabber
 * down lowers the sheet, dragging it up raises it, and tapping it switches between the two.
 */
@Composable
private fun Grabber(exploring: Boolean, onExploringChange: (Boolean) -> Unit) {
    val description = stringResource(R.string.plot_detail_sheet_toggle)
    var dragged by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(exploring) {
                detectVerticalDragGestures(
                    onDragStart = { dragged = 0f },
                    onDragEnd = {
                        if (dragged > DRAG_THRESHOLD_PX) onExploringChange(true)
                        if (dragged < -DRAG_THRESHOLD_PX) onExploringChange(false)
                    },
                    onVerticalDrag = { _, amount -> dragged += amount },
                )
            }
            .clickable(onClickLabel = description, role = Role.Button) { onExploringChange(!exploring) },
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(modifier = Modifier.padding(top = 8.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(Neutral300))
    }
}

@Composable
private fun StatusChip(isActive: Boolean) {
    val background = if (isActive) Green200 else Neutral200
    val content = if (isActive) Green800 else Neutral700
    Row(
        modifier = Modifier.clip(CircleShape).background(background).padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(content))
        Text(
            text = stringResource(if (isActive) R.string.plot_status_active else R.string.plot_status_archived),
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
        )
    }
}

/** One of the three figures under the title: a serif number over a small caption. */
@Composable
private fun PlotStatTile(value: String, caption: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = Neutral900,
            maxLines = 1,
            softWrap = false,
        )
        Text(text = caption, style = MaterialTheme.typography.bodySmall, color = Neutral600, maxLines = 1)
    }
}

@Composable
private fun PlotNotFound(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().statusBarsPadding().padding(24.dp)) {
        CircleIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.plot_detail_not_found_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.plot_detail_not_found_body),
                style = MaterialTheme.typography.bodyLarge,
                color = Neutral600,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private val previewPlot = Plot(
    id = PlotId("1"),
    name = "La Yarada 03",
    variety = OliveVariety.SEVILLANA,
    areaHectares = 0.92,
    treesPerHectare = 204,
    rowSpacingMeters = 7.0,
    treeSpacingMeters = 5.0,
    outline = listOf(GeoPoint(-18.05, -70.25), GeoPoint(-18.049, -70.24), GeoPoint(-18.06, -70.241)),
    lastPruningDate = null,
    isActive = true,
    revision = 0,
)

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 420)
@Composable
private fun DetailSheetPreview() {
    VioraTheme { DetailSheet(
            plot = previewPlot,
            exploring = false,
            onExploringChange = {},
            sheetHeight = 320.dp,
            harvest = LotHarvest(index = 0.51, missingCampaigns = 0),
            onHarvestHistory = {},
        ) }
}

@Preview(showBackground = true, backgroundColor = 0xFF1B1916)
@Composable
private fun SavedNoticePreview() {
    VioraTheme { Box(Modifier.padding(16.dp)) { SavedNotice() } }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 480)
@Composable
private fun PlotNotFoundPreview() {
    VioraTheme { PlotDetailContent(state = PlotDetailUiState.NotFound, onBack = {}) }
}
