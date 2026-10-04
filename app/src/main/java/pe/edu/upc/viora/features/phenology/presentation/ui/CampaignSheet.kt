package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignEditorUiState
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignMode
import pe.edu.upc.viora.features.phenology.presentation.state.IndexPreview
import pe.edu.upc.viora.features.phenology.presentation.state.YearCaption
import pe.edu.upc.viora.features.phenology.presentation.state.YearError
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * "Agrega una campaña" and "Corrige la campaña 2024" (Figma P41): the year, the kilos, what the
 * index would become and the save button; when correcting, also the way to delete the campaign.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignSheet(
    state: CampaignEditorUiState,
    subtitle: String,
    onYearStep: (Int) -> Unit,
    onKilosChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp).width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Neutral300)) },
    ) {
        val correcting = state.mode as? CampaignMode.Correct
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SheetHeader(state, subtitle, onDismiss)
            if (correcting == null) {
                YearCard(state, onYearStep)
            } else {
                CorrectedYearCard(correcting, state)
            }
            KilosCard(state, onKilosChange)
            state.preview?.let { PreviewCard(state, it) }
            state.error?.let {
                Text(stringResource(it.messageRes()), style = MaterialTheme.typography.bodySmall, color = Terracotta700)
            }
            PrimaryPillButton(
                text = stringResource(if (correcting == null) R.string.harvest_save_campaign else R.string.harvest_save_changes),
                onClick = onSave,
                enabled = state.canSave,
                isLoading = state.isSaving,
                trailingIcon = R.drawable.ic_check,
            )
            if (correcting != null) {
                Text(
                    text = stringResource(R.string.harvest_delete_campaign_link),
                    style = MaterialTheme.typography.titleSmall,
                    color = Terracotta700,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .clickable(enabled = !state.isSaving, role = Role.Button, onClick = onDelete)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun SheetHeader(state: CampaignEditorUiState, subtitle: String, onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            val titleStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
            val correcting = state.mode is CampaignMode.Correct
            Text(
                stringResource(if (correcting) R.string.harvest_sheet_correct_lead else R.string.harvest_sheet_add_lead),
                style = titleStyle,
                color = Neutral900,
            )
            Text(
                text = if (correcting) {
                    stringResource(R.string.harvest_sheet_correct_emphasis, state.year)
                } else {
                    stringResource(R.string.harvest_sheet_add_emphasis)
                },
                style = titleStyle.copy(fontStyle = FontStyle.Italic),
                color = Neutral900,
            )
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Neutral600, modifier = Modifier.padding(top = 4.dp))
            }
        }
        CircleIconButton(
            icon = R.drawable.ic_close,
            contentDescription = stringResource(R.string.action_close),
            onClick = onDismiss,
        )
    }
}

private fun Modifier.errorBorder(error: Boolean): Modifier =
    if (error) border(1.5.dp, Terracotta500, RoundedCornerShape(24.dp)) else this

@Composable
private fun YearCard(state: CampaignEditorUiState, onYearStep: (Int) -> Unit) {
    val hasError = state.yearError != null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .errorBorder(hasError)
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FieldLabel(stringResource(R.string.harvest_campaign_label))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            StepButton(
                icon = null,
                symbol = "−",
                description = stringResource(R.string.harvest_year_previous),
                dark = false,
                onClick = { onYearStep(-1) },
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.year.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 48.sp),
                    color = if (hasError) Terracotta700 else Neutral900,
                )
                Text(yearCaptionText(state), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp), color = if (hasError) Terracotta700 else Neutral600)
            }
            StepButton(
                icon = R.drawable.ic_add,
                symbol = null,
                description = stringResource(R.string.harvest_year_next),
                dark = !hasError,
                onClick = { onYearStep(+1) },
            )
        }
        state.yearError?.let { ErrorLine(yearErrorText(it, state)) }
    }
}

@Composable
private fun StepButton(icon: Int?, symbol: String?, description: String, dark: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (dark) Green800 else Neutral100)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = Neutral0, modifier = Modifier.size(24.dp))
        } else {
            Text(symbol.orEmpty(), style = MaterialTheme.typography.headlineMedium, color = Neutral900)
        }
    }
}

@Composable
private fun CorrectedYearCard(mode: CampaignMode.Correct, state: CampaignEditorUiState) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FieldLabel(stringResource(R.string.harvest_campaign_label))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(state.year.toString(), style = MaterialTheme.typography.displayMedium.copy(fontSize = 34.sp, lineHeight = 40.sp), color = Neutral900)
            BearingTag(mode.record.bearing, large = true)
            Text(
                stringResource(R.string.harvest_row_recorded, formatRecordDate(mode.record.recordedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral700,
            )
        }
    }
}

@Composable
private fun KilosCard(state: CampaignEditorUiState, onKilosChange: (String) -> Unit) {
    val correcting = state.mode as? CampaignMode.Correct
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .errorBorder(state.kilosError)
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FieldLabel(stringResource(R.string.harvest_kilos_label))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                value = state.kilosText,
                onValueChange = onKilosChange,
                singleLine = true,
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = NewsreaderFamily,
                    fontSize = 44.sp,
                    lineHeight = 48.sp,
                    color = if (state.kilosError) Terracotta700 else Neutral900,
                ),
                cursorBrush = SolidColor(if (state.kilosError) Terracotta500 else Green800),
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.harvest_kilos_unit),
                style = MaterialTheme.typography.bodyLarge,
                color = Neutral600,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
        Box(Modifier.fillMaxWidth().height(2.dp).background(if (state.kilosError) Terracotta500 else Green800))
        if (state.kilosError) {
            ErrorLine(stringResource(R.string.harvest_error_kilos))
        } else {
            Text(
                text = if (correcting != null) {
                    stringResource(R.string.harvest_kilos_before, formatKg(correcting.record.totalYieldKg))
                } else {
                    stringResource(R.string.harvest_kilos_hint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
    }
}

@Composable
private fun PreviewCard(state: CampaignEditorUiState, preview: IndexPreview) {
    val after = preview.after
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Harvest100).padding(start = 14.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IndexChange(before = preview.before, after = after)
        Text(
            text = previewText(state, preview),
            style = MaterialTheme.typography.bodySmall,
            color = Neutral700,
            modifier = Modifier.weight(1f),
        )
    }
}

/** "0,51 → 0,48": the serif figures, the old one quieter. [before] is null when there was no index. */
@Composable
fun IndexChange(before: Double?, after: Double?, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (before != null) {
            Text(formatIndex(before), style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp), color = Neutral600)
            Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral900, modifier = Modifier.size(18.dp))
        }
        Text(
            text = after?.let { formatIndex(it) } ?: "—",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
            color = Neutral900,
        )
    }
}

@Composable
private fun previewText(state: CampaignEditorUiState, preview: IndexPreview): String {
    val afterClass = preview.afterClass
    val sameClass = preview.beforeClass == afterClass
    val classText = afterClass?.let { stringResource(it.sentenceRes()) }.orEmpty()
    return when (state.mode) {
        CampaignMode.Add -> when {
            afterClass == null -> stringResource(R.string.harvest_preview_add_progress, preview.campaignsAfter, HoblynBbi.MIN_CAMPAIGNS)
            preview.before == null -> stringResource(R.string.harvest_preview_add_first, preview.campaignsAfter, classText)
            sameClass -> stringResource(R.string.harvest_preview_add_same, state.year, classText)
            else -> stringResource(R.string.harvest_preview_add_change, state.year, classText)
        }
        is CampaignMode.Correct -> {
            val intervals = preview.intervals.map { stringResource(R.string.harvest_interval_label, it.fromYear % 100, it.toYear % 100) }
                .joinToString(stringResource(R.string.harvest_and))
            when {
                afterClass == null || intervals.isEmpty() -> stringResource(R.string.harvest_preview_correct_plain)
                sameClass -> stringResource(R.string.harvest_preview_correct_same, intervals, classText)
                else -> stringResource(R.string.harvest_preview_correct_change, intervals, classText)
            }
        }
    }
}

@Composable
private fun yearCaptionText(state: CampaignEditorUiState): String = when (state.yearError) {
    YearError.FUTURE -> stringResource(R.string.harvest_caption_future)
    YearError.TOO_OLD -> stringResource(R.string.harvest_caption_too_old)
    YearError.DUPLICATE -> stringResource(R.string.harvest_caption_duplicate)
    null -> when (val caption = state.yearCaption) {
        YearCaption.FirstEver -> stringResource(R.string.harvest_caption_first)
        is YearCaption.BeforeOldest -> stringResource(R.string.harvest_caption_before_oldest, caption.year)
        is YearCaption.AfterLatest -> stringResource(R.string.harvest_caption_after_latest, caption.year)
        YearCaption.Between -> stringResource(R.string.harvest_caption_between)
    }
}

@Composable
private fun yearErrorText(error: YearError, state: CampaignEditorUiState): String = when (error) {
    YearError.FUTURE -> stringResource(R.string.harvest_error_future, state.year, state.minYear, state.maxYear)
    YearError.TOO_OLD -> stringResource(R.string.harvest_error_too_old, state.year, state.minYear, state.maxYear)
    YearError.DUPLICATE -> stringResource(R.string.harvest_error_duplicate, state.year)
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.88.sp), color = Neutral600)
}

@Composable
private fun ErrorLine(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
        Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = Terracotta700, modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Terracotta700)
    }
}
