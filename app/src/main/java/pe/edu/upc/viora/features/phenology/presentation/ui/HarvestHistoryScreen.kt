package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraSectionHeader
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignChangeKind
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignResult
import pe.edu.upc.viora.features.phenology.presentation.state.Headline
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestHistoryUiState
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestNotice
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestPresenter
import pe.edu.upc.viora.features.phenology.presentation.state.Outlook
import pe.edu.upc.viora.features.phenology.presentation.state.Voice
import pe.edu.upc.viora.features.phenology.presentation.viewmodel.CampaignEditorViewModel
import pe.edu.upc.viora.features.phenology.presentation.viewmodel.HarvestHistoryViewModel
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.VoiceLine

private const val NOTICE_MILLIS = 3_500L

@Composable
fun HarvestHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HarvestHistoryViewModel = hiltViewModel(),
    editor: CampaignEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val editorState by editor.uiState.collectAsStateWithLifecycle()
    var showInfo by rememberSaveable { mutableStateOf(false) }

    HarvestHistoryContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onWhatIs = { showInfo = true },
        onDismissNotice = viewModel::dismissNotice,
        onAddCampaign = editor::openAdd,
        onEditCampaign = editor::openCorrect,
        modifier = modifier,
    )

    val content = state as? HarvestHistoryUiState.Content
    val onChanged: (CampaignResult) -> Unit = { viewModel.onCampaignChanged(it.kind, it.year, it.recordId) }
    editorState?.let { sheet ->
        val subtitle = content?.let { c ->
            c.plotSubtitle?.let { stringResource(R.string.harvest_sheet_subtitle, c.plotName, formatHectares(it.areaHectares)) } ?: c.plotName
        }.orEmpty()
        CampaignSheet(
            state = sheet,
            subtitle = subtitle,
            onYearStep = editor::onYearStep,
            onKilosChange = editor::onKilosChange,
            onSave = { editor.save(onChanged) },
            onDelete = editor::requestDelete,
            onDismiss = editor::dismiss,
        )
        val deletePreview = sheet.deletePreview
        if (sheet.confirmingDelete && deletePreview != null) {
            DeleteCampaignDialog(
                year = sheet.year,
                plotName = content?.plotName.orEmpty(),
                preview = deletePreview,
                isWorking = sheet.isSaving,
                error = sheet.error,
                onConfirm = { editor.confirmDelete(onChanged) },
                onDismiss = editor::cancelDelete,
            )
        }
    }
    if (showInfo && content != null) {
        BbiInfoSheet(
            index = content.index,
            bbiClass = content.bbiClass,
            example = if (content.hasIndex) HarvestPresenter.example(content.records) else null,
            onDismiss = { showInfo = false },
        )
    }
}

@Composable
fun HarvestHistoryContent(
    state: HarvestHistoryUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onWhatIs: () -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier,
    onAddCampaign: () -> Unit = {},
    onEditCampaign: (HarvestRecord) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding(),
    ) {
        val content = state as? HarvestHistoryUiState.Content
        TopBar(content = content, onBack = onBack, onDismissNotice = onDismissNotice)
        when (state) {
            HarvestHistoryUiState.Loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green900)
            }
            is HarvestHistoryUiState.Error -> ErrorBody(state, onRetry, Modifier.weight(1f))
            is HarvestHistoryUiState.Content -> ContentBody(
                state = state,
                onRetry = onRetry,
                onWhatIs = onWhatIs,
                onAddCampaign = onAddCampaign,
                onEditCampaign = onEditCampaign,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TopBar(content: HarvestHistoryUiState.Content?, onBack: () -> Unit, onDismissNotice: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        val notice = content?.notice
        if (notice != null) {
            LaunchedEffect(notice) {
                delay(NOTICE_MILLIS)
                onDismissNotice()
            }
            NoticePill(notice)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = content?.plotName.orEmpty().ifBlank { " " },
                    style = MaterialTheme.typography.titleMedium,
                    color = Neutral900,
                    maxLines = 1,
                )
                val subtitle = content?.plotSubtitle
                Text(
                    text = if (subtitle != null) {
                        stringResource(
                            R.string.harvest_subtitle,
                            stringResource(subtitle.variety.labelRes()),
                            formatHectares(subtitle.areaHectares),
                        )
                    } else {
                        stringResource(R.string.harvest_subtitle_plain)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                    maxLines = 1,
                )
            }
        }
        // Keeps the title centred: the design's "more" button has nothing to offer here yet.
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun NoticePill(notice: HarvestNotice) {
    val text = when (notice.kind) {
        CampaignChangeKind.ADDED -> stringResource(R.string.harvest_notice_added, notice.year)
        CampaignChangeKind.CORRECTED -> stringResource(R.string.harvest_notice_corrected, notice.year)
        CampaignChangeKind.DELETED -> stringResource(R.string.harvest_notice_deleted, notice.year)
    }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Green800)
            .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(Harvest400), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Neutral900, modifier = Modifier.size(16.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = Neutral0, maxLines = 1)
    }
}

@Composable
private fun ErrorBody(state: HarvestHistoryUiState.Error, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(state.error.messageRes()),
            style = MaterialTheme.typography.bodyLarge,
            color = Terracotta700,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Terracotta100).padding(16.dp),
        )
        PillAction(stringResource(R.string.harvest_retry), onClick = onRetry, enabled = true)
    }
}

@Composable
private fun ContentBody(
    state: HarvestHistoryUiState.Content,
    onRetry: () -> Unit,
    onWhatIs: () -> Unit,
    onAddCampaign: () -> Unit,
    onEditCampaign: (HarvestRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = state.summary
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        Heading(summary.headline, summary.missing)
        if (state.offline) OfflineBanner(state, onRetry)

        val index = state.index
        val bbiClass = state.bbiClass
        if (index != null && bbiClass != null) {
            BearingGaugeCard(index = index, bbiClass = bbiClass, onWhatIs = onWhatIs)
        } else {
            ProgressCard(count = state.records.size)
        }
        VoiceLine(text = voiceText(summary.voice))
        if (index != null && bbiClass != null) InsightCards(state)

        VioraSectionHeader(title = stringResource(R.string.harvest_chart_title))
        HarvestChartCard(records = state.records, averageKg = state.averageKg, intervals = state.intervals)

        VioraSectionHeader(
            title = stringResource(R.string.harvest_registered_title),
            count = state.records.size,
            actionLabel = if (state.canEdit) stringResource(R.string.harvest_add_link) else null,
            onAction = if (state.canEdit) onAddCampaign else null,
        )
        if (state.records.isNotEmpty()) {
            CampaignList(
                records = state.records,
                newRecordId = state.newRecordId,
                enabled = state.canEdit,
                onClick = onEditCampaign,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PillAction(stringResource(R.string.harvest_add_campaign), onClick = onAddCampaign, enabled = state.canEdit)
            Text(
                text = stringResource(
                    when {
                        state.offline -> R.string.harvest_helper_offline
                        state.hasIndex -> R.string.harvest_helper_ready
                        else -> R.string.harvest_helper_insufficient
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            )
        }
        Spacer(Modifier.height(VioraTabBarDefaults.ContentBottomPadding + 8.dp))
    }
}

@Composable
private fun Heading(headline: Headline, missing: Int) {
    val (lead, emphasis) = when (headline) {
        Headline.STRONG -> stringResource(R.string.harvest_headline_lead) to stringResource(R.string.harvest_headline_strong)
        Headline.MODERATE -> stringResource(R.string.harvest_headline_lead) to stringResource(R.string.harvest_headline_moderate)
        Headline.STEADY -> stringResource(R.string.harvest_headline_lead) to stringResource(R.string.harvest_headline_steady)
        Headline.MISSING -> if (missing == 1) {
            stringResource(R.string.harvest_headline_missing_one_lead) to stringResource(R.string.harvest_headline_missing_one)
        } else {
            stringResource(R.string.harvest_headline_missing_many_lead) to stringResource(R.string.harvest_headline_missing_many, missing)
        }
    }
    Column {
        Text(lead, style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 46.sp), color = Neutral900)
        Text(
            emphasis,
            style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 46.sp, fontStyle = FontStyle.Italic),
            color = Neutral900,
        )
    }
}

@Composable
private fun voiceText(voice: Voice): String = when (voice) {
    is Voice.BreakCycle -> stringResource(R.string.harvest_voice_break_cycle, voice.year)
    is Voice.Recover -> stringResource(R.string.harvest_voice_recover, voice.year)
    Voice.Steady -> stringResource(R.string.harvest_voice_steady)
    is Voice.AskYear -> stringResource(R.string.harvest_voice_ask_year, voice.year)
}

@Composable
private fun OfflineBanner(state: HarvestHistoryUiState.Content, onRetry: () -> Unit) {
    val date = state.lastRefresh?.let { formatShortDate(it) }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Neutral900)
            .clickable(role = Role.Button, onClick = onRetry)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_cloud_sync), contentDescription = null, tint = Neutral0, modifier = Modifier.size(18.dp))
        Text(
            text = if (date != null) stringResource(R.string.harvest_offline_banner, date) else stringResource(R.string.harvest_offline_banner_plain),
            style = MaterialTheme.typography.labelLarge,
            color = Neutral0,
        )
    }
}

/** "2 of 3 campaigns needed" (Figma "Historial insuficiente"). */
@Composable
private fun ProgressCard(count: Int) {
    val needed = HoblynBbi.MIN_CAMPAIGNS
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Green800).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.harvest_history_eyebrow),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.88.sp),
            color = Neutral0.copy(alpha = 0.78f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp, lineHeight = 100.sp),
                color = Neutral0,
            )
            Text(
                text = stringResource(R.string.harvest_progress_of, needed),
                style = MaterialTheme.typography.headlineMedium.copy(fontStyle = FontStyle.Italic),
                color = Neutral0,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(needed) { step ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(if (step < count) Harvest300 else Neutral0.copy(alpha = 0.2f)),
                )
            }
        }
        Text(
            text = pluralStringResource(R.plurals.harvest_insufficient_body, (needed - count).coerceAtLeast(1), (needed - count).coerceAtLeast(1)),
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral50,
        )
    }
}

@Composable
private fun InsightCards(state: HarvestHistoryUiState.Content) {
    val summary = state.summary
    val drop = summary.dropAfterOn
    val ordered = state.records.sortedBy { it.campaignYear }
    val countCard: @Composable (Modifier) -> Unit = { m ->
        CampaignsCard(ordered, m)
    }
    val nextCard: @Composable (Modifier) -> Unit = { m -> NextCampaignCard(summary.nextYear, summary.outlook, m) }
    if (drop != null) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AfterOnCard(drop.ratio, drop.fromKg, drop.afterKg, Modifier.weight(1f).fillMaxHeight())
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                countCard(Modifier.weight(1f))
                nextCard(Modifier.weight(1f))
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
            countCard(Modifier.weight(1f).fillMaxHeight())
            nextCard(Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
private fun AfterOnCard(ratio: Double, fromKg: Double, afterKg: Double, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(28.dp)).background(Terracotta100).padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(stringResource(R.string.harvest_after_on), style = MaterialTheme.typography.labelMedium, color = Terracotta700)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                Box(Modifier.size(width = 16.dp, height = 48.dp).clip(RoundedCornerShape(8.dp)).background(Terracotta500))
                Box(Modifier.size(16.dp).clip(CircleShape).background(Terracotta500.copy(alpha = 0.4f)))
            }
        }
        Text(
            text = formatPercent(ratio),
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp, lineHeight = 60.sp, letterSpacing = (-1.5).sp),
            color = Terracotta700,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.harvest_after_on_body), style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp, lineHeight = 18.sp), color = Terracotta700)
            Row(
                modifier = Modifier.clip(CircleShape).background(Neutral0).padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Terracotta500))
                Text(
                    stringResource(R.string.harvest_after_on_pill, formatKg(fromKg), formatKg(afterKg)),
                    style = MaterialTheme.typography.labelMedium,
                    color = Terracotta700,
                )
            }
        }
    }
}

@Composable
private fun CampaignsCard(ordered: List<HarvestRecord>, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(ordered.size.toString(), style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp, lineHeight = 52.sp), color = Neutral900)
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    ordered.takeLast(6).forEach { Box(Modifier.size(9.dp).clip(CircleShape).background(it.bearing.barColor())) }
                }
            }
            Text(
                pluralStringResource(R.plurals.harvest_campaigns_label, ordered.size),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                color = Neutral700,
            )
            Text(
                stringResource(R.string.harvest_year_range, ordered.first().campaignYear, ordered.last().campaignYear),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral700,
            )
        }
    }
}

@Composable
private fun NextCampaignCard(year: Int?, outlook: Outlook?, modifier: Modifier = Modifier) {
    if (year == null || outlook == null) return
    Column(
        modifier = modifier.clip(RoundedCornerShape(28.dp)).background(Harvest100).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(year.toString(), style = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp, lineHeight = 44.sp), color = Neutral900)
        Text(
            text = stringResource(
                when (outlook) {
                    Outlook.OFF_LIKELY -> R.string.harvest_next_off
                    Outlook.ON_LIKELY -> R.string.harvest_next_on
                    Outlook.STEADY -> R.string.harvest_next_steady
                },
            ),
            style = MaterialTheme.typography.labelMedium,
            color = Harvest800,
        )
    }
}

@Composable
private fun CampaignList(records: List<HarvestRecord>, newRecordId: String?, enabled: Boolean, onClick: (HarvestRecord) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(horizontal = 18.dp, vertical = 4.dp),
    ) {
        records.forEachIndexed { position, record ->
            val isNew = record.id == newRecordId
            CampaignRow(record, isNew = isNew, enabled = enabled, onClick = { onClick(record) })
            if (position < records.lastIndex && !isNew) {
                HorizontalDivider(color = Neutral200)
            }
        }
    }
}

@Composable
private fun CampaignRow(record: HarvestRecord, isNew: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isNew) Modifier.clip(RoundedCornerShape(20.dp)).background(Harvest100) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = if (isNew) 12.dp else 0.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = record.campaignYear.toString(),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 30.sp),
            color = Neutral900,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(R.string.harvest_kg_value, formatKg(record.totalYieldKg)),
                style = MaterialTheme.typography.titleSmall,
                color = Neutral900,
            )
            Text(
                text = if (isNew) stringResource(R.string.harvest_row_new) else stringResource(R.string.harvest_row_recorded, formatRecordDate(record.recordedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
        BearingTag(record.bearing, large = true, onHighlight = isNew)
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = if (enabled) Neutral600 else Neutral300,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PillAction(text: String, onClick: () -> Unit, enabled: Boolean) {
    PrimaryPillButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        trailingIcon = R.drawable.ic_add,
    )
}
