package pe.edu.upc.viora.features.telemetry.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import kotlinx.coroutines.launch
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest700
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderItalic
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSection
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSectionsSheet
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatCount
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorStatus
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.presentation.state.LinkNodeUiState
import pe.edu.upc.viora.features.telemetry.presentation.state.SensorsUiState
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.LinkNodeViewModel
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.SensorsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onHarvestHistory: () -> Unit = {},
    viewModel: SensorsViewModel = hiltViewModel(),
    linkViewModel: LinkNodeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val linkState by linkViewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var showMore by rememberSaveable { mutableStateOf(false) }

    val plotName = when (val s = state) {
        is SensorsUiState.Content -> s.plotName
        is SensorsUiState.Empty -> s.plotName
        else -> ""
    }

    SensorsScreenContent(
        state = state,
        onBack = onBack,
        onLinkNode = {
            linkViewModel.reset()
            showSheet = true
        },
        onMore = { showMore = true },
        modifier = modifier,
    )

    val plot = when (val s = state) {
        is SensorsUiState.Content -> s.plot
        is SensorsUiState.Empty -> s.plot
        else -> null
    }
    if (showMore) {
        LotSectionsSheet(
            plotName = plotName,
            summary = plot?.let {
                stringResource(
                    R.string.plot_options_subtitle,
                    stringResource(it.variety.labelRes()),
                    formatHectares(it.areaHectares),
                    formatCount(it.estimatedTrees),
                )
            }.orEmpty(),
            current = LotSection.SENSORS,
            onSelect = { section ->
                showMore = false
                if (section == LotSection.HARVEST) onHarvestHistory()
            },
            onDismiss = { showMore = false },
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Neutral50,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        ) {
            LinkNodeSheet(
                plotName = plotName,
                state = linkState,
                onNameChange = linkViewModel::onNameChange,
                onTypeChange = linkViewModel::onTypeChange,
                onDepthChange = linkViewModel::onDepthChange,
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                },
                onSubmit = {
                    linkViewModel.submit {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showSheet = false
                            viewModel.refresh()
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun SensorsScreenContent(
    state: SensorsUiState,
    onBack: () -> Unit,
    onLinkNode: () -> Unit,
    modifier: Modifier = Modifier,
    onMore: () -> Unit = {},
) {
    val plotName = when (state) {
        is SensorsUiState.Content -> state.plotName
        is SensorsUiState.Empty -> state.plotName
        else -> ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding(),
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(
                icon = R.drawable.ic_arrow_back,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack,
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = plotName.ifBlank { " " },
                    style = MaterialTheme.typography.titleMedium,
                    color = Neutral900,
                )
                Text(
                    text = stringResource(R.string.sensors_subtitle_top),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                )
            }

            CircleIconButton(
                icon = R.drawable.ic_more_vert,
                contentDescription = stringResource(R.string.plot_menu_more),
                onClick = onMore,
            )
        }

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Main Display Title: "Tus" \n "sensores."
            Text(
                text = stringResource(R.string.sensors_title_lead),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, lineHeight = 40.sp),
                color = Neutral900,
            )
            Text(
                text = stringResource(R.string.sensors_title_emphasis),
                style = MaterialTheme.typography.displaySmall
                    .copy(fontSize = 36.sp, lineHeight = 40.sp)
                    .merge(NewsreaderItalic),
                color = Neutral900,
            )

            Spacer(Modifier.height(16.dp))

            // Virtual nodes explanation notice
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_virtual_node),
                    contentDescription = null,
                    tint = Neutral700,
                    modifier = Modifier.size(20.dp).padding(top = 2.dp),
                )
                Text(
                    text = stringResource(R.string.sensors_virtual_notice),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        lineHeight = 20.sp,
                    ),
                    color = Neutral700,
                )
            }

            Spacer(Modifier.height(20.dp))

            when (state) {
                SensorsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Green900)
                    }
                }
                is SensorsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Terracotta100)
                            .padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(state.error.messageRes()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Terracotta700,
                        )
                    }
                }
                is SensorsUiState.Empty -> {
                    Text(
                        text = stringResource(R.string.sensors_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Neutral600,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
                is SensorsUiState.Content -> {
                    // Summary status chips
                    val activeCount = state.nodes.count { it.status == SensorStatus.ACTIVE }
                    val pausedCount = state.nodes.count { it.status == SensorStatus.PAUSED }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 16.dp),
                    ) {
                        if (activeCount > 0) {
                            StatusSummaryChip(
                                text = stringResource(R.string.sensors_chip_active, activeCount),
                                dotColor = Neutral700,
                                background = Green200,
                                textColor = Neutral900,
                            )
                        }
                        if (pausedCount > 0) {
                            StatusSummaryChip(
                                text = stringResource(R.string.sensors_chip_paused, pausedCount),
                                dotColor = Harvest700,
                                background = Harvest100,
                                textColor = Harvest800,
                            )
                        }
                    }

                    // List of Sensor Cards
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        state.nodes.forEach { node ->
                            SensorCard(node = node)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action: "Vincular un nodo" button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Green900)
                    .clickable(role = Role.Button, onClick = onLinkNode)
                    .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.sensors_link_action),
                    style = MaterialTheme.typography.titleMedium,
                    color = Neutral50,
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Harvest300),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        tint = Neutral900,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Footnote
            Text(
                text = stringResource(R.string.sensors_pause_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            // Padding to ensure content is visible above floating tab bar
            Spacer(Modifier.height(VioraTabBarDefaults.ContentBottomPadding + 24.dp))
        }
    }
}

/** Card for one sensor node (US14). */
@Composable
private fun SensorCard(node: SensorNode, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .clickable(role = Role.Button) { /* Node detail/calibration */ }
            .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Circular Icon
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (node.status == SensorStatus.ACTIVE) Green200 else Neutral100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    if (node.type == SensorType.MICROCLIMA) R.drawable.ic_sensors
                    else R.drawable.ic_water_drop,
                ),
                contentDescription = null,
                tint = Neutral900,
                modifier = Modifier.size(24.dp),
            )
        }

        // Details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = Neutral900,
            )
            Text(
                text = if (node.type == SensorType.MICROCLIMA) {
                    stringResource(R.string.sensors_type_microclima_subtitle)
                } else {
                    stringResource(R.string.sensors_type_sonda_subtitle, node.depthCm ?: 30)
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )

            Spacer(Modifier.height(4.dp))

            // Status chip
            if (node.status == SensorStatus.PAUSED) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Harvest100)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Harvest700))
                    Text(
                        text = stringResource(R.string.sensors_paused_since, "el 20 oct"),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = Harvest800,
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Green200)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Neutral700))
                    Text(
                        text = stringResource(R.string.sensors_active_since, "10 min"),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = Neutral900,
                    )
                }
            }
        }

        // Measurement Values & Chevron
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (node.status == SensorStatus.ACTIVE) {
                val temp = node.lastTemperatureCelsius
                val hum = node.lastHumidityPercent
                val locale = LocalConfiguration.current.locales[0]
                val readingText = when {
                    temp != null && hum != null -> {
                        String.format(locale, "%.1f° · %.0f %%", temp, hum)
                    }
                    hum != null -> {
                        String.format(locale, "%.0f %%", hum)
                    }
                    else -> null
                }
                if (readingText != null) {
                    Text(
                        text = readingText,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        color = Neutral900,
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Neutral600,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Status summary chip at the top of the list (e.g. "• 2 activos"). */
@Composable
private fun StatusSummaryChip(
    text: String,
    dotColor: Color,
    background: Color,
    textColor: Color,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(background)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(dotColor))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
            color = textColor,
        )
    }
}

/** Circular icon button used for back and more-options buttons. */
@Composable
private fun CircleIconButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = Neutral900,
            modifier = Modifier.size(22.dp),
        )
    }
}

// ─── Modal Bottom Sheet: Link Node (US13) ───────────────────────────────────

@Composable
fun LinkNodeSheet(
    plotName: String,
    state: LinkNodeUiState,
    onNameChange: (String) -> Unit,
    onTypeChange: (SensorType) -> Unit,
    onDepthChange: (Int) -> Unit,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Top row with title and close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.sensors_link_title_lead),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp, lineHeight = 36.sp),
                    color = Neutral900,
                )
                Text(
                    text = stringResource(R.string.sensors_link_title_emphasis),
                    style = MaterialTheme.typography.displaySmall
                        .copy(fontSize = 32.sp, lineHeight = 36.sp)
                        .merge(NewsreaderItalic),
                    color = Neutral900,
                )
                Text(
                    text = stringResource(R.string.sensors_link_sheet_subtitle, plotName),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            CircleIconButton(
                icon = R.drawable.ic_close,
                contentDescription = stringResource(R.string.action_close),
                onClick = onClose,
            )
        }

        // Node name input field
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.sensors_link_name_label)) },
            singleLine = true,
            isError = state.nameError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Neutral0,
                unfocusedContainerColor = Neutral0,
                focusedBorderColor = Green900,
                unfocusedBorderColor = Color.Transparent,
                errorContainerColor = Neutral0,
            ),
        )

        // Type selection
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.sensors_link_type_label),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = Neutral600,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoicePill(
                    text = stringResource(R.string.sensors_type_microclima),
                    selected = state.type == SensorType.MICROCLIMA,
                    onClick = { onTypeChange(SensorType.MICROCLIMA) },
                )
                ChoicePill(
                    text = stringResource(R.string.sensors_type_sonda),
                    selected = state.type == SensorType.SONDA_SUELO,
                    onClick = { onTypeChange(SensorType.SONDA_SUELO) },
                )
            }
        }

        // Depth selection (animated, only visible for SONDA_SUELO)
        AnimatedVisibility(
            visible = state.type == SensorType.SONDA_SUELO,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.sensors_link_depth_label),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = Neutral600,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChoicePill(
                        text = stringResource(R.string.sensors_depth_30),
                        selected = state.depthCm == 30,
                        onClick = { onDepthChange(30) },
                    )
                    ChoicePill(
                        text = stringResource(R.string.sensors_depth_60),
                        selected = state.depthCm == 60,
                        onClick = { onDepthChange(60) },
                    )
                }
                Text(
                    text = stringResource(R.string.sensors_depth_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                )
            }
        }

        // Error message if any
        if (state.error != null) {
            Text(
                text = stringResource(state.error.messageRes()),
                style = MaterialTheme.typography.bodySmall,
                color = Terracotta700,
            )
        }

        // Submit action button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(percent = 50))
                .background(if (state.canSubmit) Green900 else Neutral300)
                .clickable(enabled = state.canSubmit, role = Role.Button, onClick = onSubmit)
                .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.sensors_link_submit),
                style = MaterialTheme.typography.titleMedium,
                color = Neutral50,
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (state.canSubmit) Harvest300 else Neutral200),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Neutral900,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = Neutral900,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoicePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
        color = if (selected) Neutral50 else Neutral900,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) Green900 else Neutral0)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

// ─── Previews ────────────────────────────────────────────────────────────────

private val previewNodes = listOf(
    SensorNode(
        id = "1",
        plotId = "p1",
        name = "Estación La Yarada",
        type = SensorType.MICROCLIMA,
        depthCm = null,
        status = SensorStatus.ACTIVE,
        lastReadingAt = "2026-10-04T05:00:00Z",
        lastTemperatureCelsius = 27.4,
        lastHumidityPercent = 38.0,
    ),
    SensorNode(
        id = "2",
        plotId = "p1",
        name = "Sonda Sector Norte",
        type = SensorType.SONDA_SUELO,
        depthCm = 30,
        status = SensorStatus.ACTIVE,
        lastReadingAt = "2026-10-04T05:00:00Z",
        lastTemperatureCelsius = null,
        lastHumidityPercent = 34.0,
    ),
    SensorNode(
        id = "3",
        plotId = "p1",
        name = "Sonda Sector Sur",
        type = SensorType.SONDA_SUELO,
        depthCm = 60,
        status = SensorStatus.PAUSED,
        lastReadingAt = "2026-10-20T00:00:00Z",
        lastTemperatureCelsius = null,
        lastHumidityPercent = null,
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 800)
@Composable
private fun SensorsScreenPreview() {
    VioraTheme {
        SensorsScreenContent(
            state = SensorsUiState.Content(
                plotName = "La Yarada 02",
                nodes = previewNodes,
                isRefreshing = false,
                refreshError = null,
            ),
            onBack = {},
            onLinkNode = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1)
@Composable
private fun LinkNodeSheetPreview() {
    VioraTheme {
        LinkNodeSheet(
            plotName = "La Yarada 02",
            state = LinkNodeUiState(
                name = "Sonda Sector Oeste",
                type = SensorType.SONDA_SUELO,
                depthCm = 30,
            ),
            onNameChange = {},
            onTypeChange = {},
            onDepthChange = {},
            onClose = {},
            onSubmit = {},
        )
    }
}
