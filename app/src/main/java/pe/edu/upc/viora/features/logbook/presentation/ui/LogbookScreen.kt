package pe.edu.upc.viora.features.logbook.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDate
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraEmptyState
import pe.edu.upc.viora.core.designsystem.component.VioraLoadingState
import pe.edu.upc.viora.core.designsystem.component.VioraSectionHeader
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.core.presentation.formatKg
import pe.edu.upc.viora.core.presentation.formatWhole
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.harvestsettlement.application.published.SettlementStatus
import pe.edu.upc.viora.core.designsystem.component.VioraGreetingHeader
import pe.edu.upc.viora.core.designsystem.component.VioraHeadline
import pe.edu.upc.viora.features.logbook.presentation.state.LogbookFilter
import pe.edu.upc.viora.features.logbook.presentation.state.LogbookGroup
import pe.edu.upc.viora.features.logbook.presentation.state.LogbookPeriod
import pe.edu.upc.viora.features.logbook.presentation.state.LogbookRowType
import pe.edu.upc.viora.features.logbook.presentation.state.LogbookUiState
import pe.edu.upc.viora.features.logbook.presentation.state.SettledHarvestEntry
import pe.edu.upc.viora.features.logbook.presentation.viewmodel.LogbookViewModel
import pe.edu.upc.viora.features.plotmanagement.application.published.FieldNoteSyncState
import pe.edu.upc.viora.features.plotmanagement.application.published.FieldNoteType

private val ScreenPadding = 24.dp

/**
 * Logbook / "Bitácora" (Figma P50): greeting, headline, filters, active in-progress sampling
 * and settled campaigns, field samplings and thinnings grouped by period. A settled row opens its P73 receipt.
 */
@Composable
fun LogbookScreen(
    onOpenSettlement: (plotId: String, year: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LogbookViewModel = hiltViewModel(),
    onContinueSampling: (plotId: String, plotName: String) -> Unit = { _, _ -> },
    onOpenAlerts: () -> Unit = {},
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val hasActiveAlerts = viewModel.hasActiveAlerts.collectAsStateWithLifecycle().value
    LogbookContent(
        state = state,
        onSelectFilter = viewModel::selectFilter,
        onRefresh = viewModel::refresh,
        onOpenSettlement = onOpenSettlement,
        onContinueSampling = onContinueSampling,
        onOpenAlerts = onOpenAlerts,
        hasActiveAlerts = hasActiveAlerts,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogbookContent(
    state: LogbookUiState,
    onSelectFilter: (LogbookFilter) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettlement: (plotId: String, year: Int) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    onContinueSampling: (plotId: String, plotName: String) -> Unit = { _, _ -> },
    onOpenAlerts: () -> Unit = {},
    hasActiveAlerts: Boolean = false,
) {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val content = state as? LogbookUiState.Content
    PullToRefreshBox(
        isRefreshing = content?.isRefreshing == true,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize().background(Neutral100),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = systemBars.calculateTopPadding() + 12.dp,
                    bottom = VioraTabBarDefaults.ContentBottomPadding + systemBars.calculateBottomPadding(),
                ),
        ) {
            VioraGreetingHeader(
                date = today,
                isOffline = content?.offline == true,
                lastRefresh = content?.lastPlotRefresh,
                onOpenAlerts = onOpenAlerts,
                hasUnreadAlerts = hasActiveAlerts,
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                VioraHeadline(
                    lead = stringResource(R.string.logbook_headline_lead),
                    emphasis = stringResource(R.string.logbook_headline_emphasis),
                )
                if (content != null && content.pendingLocalCount > 0) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Harvest100)
                            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Neutral0),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cloud_sync),
                                contentDescription = null,
                                tint = Harvest800,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = stringResource(R.string.logbook_saved_on_phone_badge, content.pendingLocalCount),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            ),
                            color = Harvest800,
                        )
                    }
                }
            }
            if (content == null) {
                VioraLoadingState(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp))
            } else {
                FilterRow(selected = content.filter, onSelect = onSelectFilter, modifier = Modifier.padding(top = 20.dp))
                content.refreshError?.let { RefreshNotice(it, onRetry = onRefresh) }

                val active = content.activeSampling
                if ((content.filter == LogbookFilter.ALL || content.filter == LogbookFilter.SAMPLINGS) && active != null) {
                    Box(modifier = Modifier.padding(top = 16.dp)) {
                        ActiveSamplingCard(
                            plotName = active.plotName,
                            completedTrees = active.completedTrees,
                            targetTrees = active.targetTrees,
                            onContinueRound = { onContinueSampling(active.plotId, active.plotName) },
                            modifier = Modifier.padding(horizontal = ScreenPadding),
                        )
                    }
                }

                if (content.isEmpty) {
                    EmptyState(content.filter)
                } else {
                    content.groups.forEach { Group(it, onOpenSettlement) }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: LogbookFilter, onSelect: (LogbookFilter) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LogbookFilter.entries.forEach { filter ->
            FilterChip(
                text = stringResource(filter.labelRes()),
                selected = filter == selected,
                onClick = { onSelect(filter) },
            )
        }
    }
}

/** Dark when selected, white when not (Figma P50 "Filtros"). */
@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp),
        color = if (selected) Neutral50 else Neutral900,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Green900 else Neutral0)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

/** Non-blocking: the cached list stays below it. Tapping it tries again. */
@Composable
private fun RefreshNotice(error: AppError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Terracotta100)
            .clickable(role = Role.Button, onClick = onRetry)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = stringResource(error.messageRes()), style = MaterialTheme.typography.bodyMedium, color = Terracotta700)
        Text(text = stringResource(R.string.logbook_showing_saved_data), style = MaterialTheme.typography.bodySmall, color = Terracotta700)
    }
}

@Composable
private fun EmptyState(filter: LogbookFilter) {
    val (title, body) = when (filter) {
        LogbookFilter.SAMPLINGS -> R.string.logbook_empty_samplings_title to R.string.logbook_empty_samplings_body
        LogbookFilter.THINNINGS -> R.string.logbook_empty_thinnings_title to R.string.logbook_empty_thinnings_body
        LogbookFilter.ALL, LogbookFilter.HARVESTS -> R.string.logbook_empty_harvests_title to R.string.logbook_empty_harvests_body
    }
    VioraEmptyState(
        title = stringResource(title),
        body = stringResource(body),
        modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 40.dp),
    )
}

@Composable
private fun Group(group: LogbookGroup, onOpenSettlement: (plotId: String, year: Int) -> Unit) {
    Column(modifier = Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        VioraSectionHeader(
            title = stringResource(group.period.titleRes()),
            modifier = Modifier.padding(horizontal = ScreenPadding),
        )
        group.entries.forEach { entry ->
            when (entry.type) {
                LogbookRowType.HARVEST -> SettledHarvestRow(
                    entry = entry,
                    onClick = { onOpenSettlement(entry.plotId, entry.campaignYear) },
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                )
                LogbookRowType.SAMPLING -> SamplingEventRow(entry, modifier = Modifier.padding(horizontal = ScreenPadding))
                LogbookRowType.THINNING -> ThinningEventRow(entry, modifier = Modifier.padding(horizontal = ScreenPadding))
                LogbookRowType.NOTE -> NoteRow(entry, modifier = Modifier.padding(horizontal = ScreenPadding))
            }
        }
    }
}

/** "Cosecha asentada" row (Figma P50 "Registro"): icon, title, "plot · campaign · kg", check. */
@Composable
private fun SettledHarvestRow(entry: SettledHarvestEntry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val plot = entry.plotName ?: stringResource(R.string.logbook_unknown_plot)
    val details = stringResource(R.string.logbook_harvest_details, plot, entry.campaignYear, formatKg(entry.totalYieldKg))
    val subtitle = if (entry.status == SettlementStatus.AUDITED) {
        stringResource(R.string.logbook_harvest_audited, details)
    } else {
        details
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Harvest100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_inventory),
                contentDescription = null,
                tint = Harvest800,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.logbook_harvest_title),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
                color = Neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_task_alt),
            contentDescription = null,
            tint = Green800,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** "Muestreo completado" row: icon, title, "plot · trees · fruits/shoot", check. */
@Composable
private fun SamplingEventRow(entry: SettledHarvestEntry, modifier: Modifier = Modifier) {
    val plot = entry.plotName ?: stringResource(R.string.logbook_unknown_plot)
    val trees = entry.evaluatedTreesCount ?: 0
    val mean = formatDecimal(entry.meanFruitsPerShoot ?: 0.0, 2)
    val details = stringResource(R.string.logbook_sampling_completed_desc, plot, trees, mean)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Green200),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_nutrition),
                contentDescription = null,
                tint = Green900,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.logbook_sampling_completed_title),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
                color = Neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_task_alt),
            contentDescription = null,
            tint = Green800,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** "Raleo registrado" row: icon, title, "plot · removal% · timeliness", check. */
@Composable
private fun ThinningEventRow(entry: SettledHarvestEntry, modifier: Modifier = Modifier) {
    val plot = entry.plotName ?: stringResource(R.string.logbook_unknown_plot)
    val percent = formatWhole(entry.removalPercentage ?: 0.0)
    // The backend sends OPTIMAL or LATE; the producer reads words, never the code.
    val timeliness = when (entry.timeliness) {
        "OPTIMAL" -> stringResource(R.string.logbook_thinning_on_time)
        "LATE" -> stringResource(R.string.logbook_thinning_late)
        else -> entry.timeliness.orEmpty()
    }
    val details = stringResource(R.string.logbook_thinning_executed_desc, plot, percent, timeliness)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Terracotta100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_content_cut),
                contentDescription = null,
                tint = Terracotta600,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.logbook_thinning_executed_title),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
                color = Neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_task_alt),
            contentDescription = null,
            tint = Green800,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * "Nota de riego" row (Figma P50 "Nota guardada"): "plot · text", then a check once on the server,
 * a cloud while it waits on the phone, or a warning when the server refused it.
 */
@Composable
private fun NoteRow(entry: SettledHarvestEntry, modifier: Modifier = Modifier) {
    val plot = entry.plotName ?: stringResource(R.string.logbook_unknown_plot)
    val type = entry.noteType ?: FieldNoteType.OTHER
    val title = stringResource(R.string.logbook_note_title, stringResource(type.titleRes()))
    val details = when (entry.noteSyncState) {
        FieldNoteSyncState.FAILED -> stringResource(R.string.logbook_note_failed)
        else -> stringResource(R.string.logbook_note_details, plot, entry.noteText.orEmpty().replaceFirstChar { it.lowercase() })
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(start = 10.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Neutral100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit_note),
                contentDescription = null,
                tint = Neutral900,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
                color = Neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = if (entry.noteSyncState == FieldNoteSyncState.FAILED) Terracotta700 else Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val (icon, tint, description) = when (entry.noteSyncState) {
            FieldNoteSyncState.PENDING -> Triple(R.drawable.ic_cloud_sync, Harvest800, stringResource(R.string.logbook_note_pending))
            FieldNoteSyncState.FAILED -> Triple(R.drawable.ic_warning, Terracotta700, stringResource(R.string.logbook_note_failed))
            else -> Triple(R.drawable.ic_task_alt, Green800, null)
        }
        Icon(painter = painterResource(icon), contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
    }
}

private fun FieldNoteType.titleRes(): Int = when (this) {
    FieldNoteType.IRRIGATION -> R.string.note_type_irrigation_title
    FieldNoteType.PRUNING -> R.string.note_type_pruning_title
    FieldNoteType.FERTILIZATION -> R.string.note_type_fertilization_title
    FieldNoteType.PESTS -> R.string.note_type_pests_title
    FieldNoteType.OTHER -> R.string.note_type_other_title
}

private fun LogbookFilter.labelRes(): Int = when (this) {
    LogbookFilter.ALL -> R.string.logbook_filter_all
    LogbookFilter.SAMPLINGS -> R.string.logbook_filter_samplings
    LogbookFilter.THINNINGS -> R.string.logbook_filter_thinnings
    LogbookFilter.HARVESTS -> R.string.logbook_filter_harvests
}

private fun LogbookPeriod.titleRes(): Int = when (this) {
    LogbookPeriod.THIS_WEEK -> R.string.logbook_period_this_week
    LogbookPeriod.THIS_MONTH -> R.string.logbook_period_this_month
    LogbookPeriod.EARLIER -> R.string.logbook_period_earlier
}

private val previewEntries = listOf(
    SettledHarvestEntry("1", "p1", "La Yarada 02", 2025, 24_000.0, SettlementStatus.SETTLED, Instant.parse("2026-11-17T10:00:00Z")),
    SettledHarvestEntry("2", "p2", null, 2024, 7_750.0, SettlementStatus.AUDITED, Instant.parse("2026-11-16T10:00:00Z")),
)

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 800)
@Composable
private fun LogbookPreview() {
    VioraTheme {
        LogbookContent(
            state = LogbookUiState.Content(
                filter = LogbookFilter.ALL,
                groups = listOf(LogbookGroup(LogbookPeriod.THIS_WEEK, previewEntries)),
                refreshError = AppError.Offline,
                lastPlotRefresh = null,
                isRefreshing = false,
            ),
            onSelectFilter = {},
            onRefresh = {},
            onOpenSettlement = { _, _ -> },
            today = LocalDate.of(2026, 11, 18),
        )
    }
}
