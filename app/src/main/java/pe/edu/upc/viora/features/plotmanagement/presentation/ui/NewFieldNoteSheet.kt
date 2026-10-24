package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteType
import pe.edu.upc.viora.features.plotmanagement.presentation.state.NewFieldNoteUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.NewFieldNoteViewModel

/**
 * P75 · Nueva nota (hoja): plot, type, free text and an optional photo. Saving keeps the note on
 * the phone and sends it when there is signal; [onSaved] then takes the producer to the logbook.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFieldNoteSheet(
    onSaved: (FieldNote) -> Unit,
    onDismiss: () -> Unit,
    viewModel: NewFieldNoteViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.saved.collect(onSaved) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.attachPhoto(uri.toString())
    }
    val close = {
        viewModel.discardDraft()
        onDismiss()
    }
    ModalBottomSheet(
        onDismissRequest = close,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(Neutral300)) },
    ) {
        NewFieldNoteContent(
            state = state,
            onSelectPlot = viewModel::selectPlot,
            onSelectType = viewModel::selectType,
            onTextChange = viewModel::changeText,
            onAddPhoto = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onRemovePhoto = viewModel::removePhoto,
            onSave = viewModel::save,
            onClose = close,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewFieldNoteContent(
    state: NewFieldNoteUiState,
    onSelectPlot: (String) -> Unit,
    onSelectType: (FieldNoteType) -> Unit,
    onTextChange: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val day = DateTimeFormatter.ofPattern(stringResource(R.string.note_date_pattern), locale).format(state.today)
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val title = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
                Column {
                    Text(stringResource(R.string.note_title_lead), style = title, color = Neutral900)
                    Text(stringResource(R.string.note_title_emphasis), style = title, fontStyle = FontStyle.Italic, color = Neutral900)
                }
                Text(
                    stringResource(R.string.note_today, day),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = Neutral600,
                )
            }
            CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onClose)
        }

        Eyebrow(R.string.note_plot_label, Modifier.padding(top = 20.dp))
        FlowRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.plots.forEach { plot ->
                NoteChip(plot.name, selected = plot.id.value == state.plotId, onClick = { onSelectPlot(plot.id.value) })
            }
        }
        if (state.plots.isEmpty()) {
            Text(stringResource(R.string.note_no_plots), style = MaterialTheme.typography.bodySmall, color = Neutral600, modifier = Modifier.padding(top = 8.dp))
        }

        Eyebrow(R.string.note_type_label, Modifier.padding(top = 20.dp))
        FlowRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldNoteType.entries.forEach { type ->
                NoteChip(stringResource(type.labelRes()), selected = type == state.type, onClick = { onSelectType(type) })
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Neutral0)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.note_text_label), style = MaterialTheme.typography.bodySmall, color = Neutral600)
            BasicTextField(
                value = state.text,
                onValueChange = onTextChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 24.sp, color = Neutral900),
                cursorBrush = SolidColor(Green900),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                decorationBox = { field ->
                    Box {
                        if (state.text.isEmpty()) {
                            Text(stringResource(R.string.note_text_hint), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 24.sp), color = Neutral600)
                        }
                        field()
                    }
                },
            )
            Text(
                stringResource(R.string.note_text_counter, state.text.length, FieldNote.MAX_TEXT_LENGTH),
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600,
                modifier = Modifier.align(Alignment.End),
            )
        }

        PhotoPill(state = state, onAdd = onAddPhoto, onRemove = onRemovePhoto, modifier = Modifier.padding(top = 16.dp))
        if (state.photoFailed) {
            Text(stringResource(R.string.note_photo_failed), style = MaterialTheme.typography.bodySmall, color = Terracotta700, modifier = Modifier.padding(top = 8.dp))
        }

        PrimaryPillButton(
            text = stringResource(R.string.note_save),
            onClick = onSave,
            trailingIcon = R.drawable.ic_check,
            enabled = state.canSave,
            isLoading = state.isSaving,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}

@Composable
private fun Eyebrow(@StringRes text: Int, modifier: Modifier = Modifier) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 1.2.sp),
        color = Neutral600,
        modifier = modifier,
    )
}

/** Dark when selected, white when not, like the logbook filters. */
@Composable
private fun NoteChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp),
        color = if (selected) Neutral50 else Neutral900,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) Green900 else Neutral0)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

/** "Agregar foto (opcional)", or the attached photo with a way to remove it. */
@Composable
private fun PhotoPill(state: NewFieldNoteUiState, onAdd: () -> Unit, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    val attached = state.photoPath != null
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Neutral0)
            .clickable(enabled = !state.isPreparingPhoto, role = Role.Button, onClick = if (attached) onRemove else onAdd)
            .padding(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isPreparingPhoto) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Green900)
        } else {
            Icon(
                painterResource(if (attached) R.drawable.ic_check else R.drawable.ic_photo_camera),
                contentDescription = null,
                tint = if (attached) Green800 else Neutral900,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            stringResource(if (attached) R.string.note_photo_attached else R.string.note_photo_add),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp),
            color = Neutral900,
        )
    }
}

/** The word the producer reads for each type (P75 chips, logbook rows). */
fun FieldNoteType.labelRes(): Int = when (this) {
    FieldNoteType.IRRIGATION -> R.string.note_type_irrigation
    FieldNoteType.PRUNING -> R.string.note_type_pruning
    FieldNoteType.FERTILIZATION -> R.string.note_type_fertilization
    FieldNoteType.PESTS -> R.string.note_type_pests
    FieldNoteType.OTHER -> R.string.note_type_other
}
