package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import java.io.File
import java.time.format.DateTimeFormatter
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.VioraBottomSheet
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncState
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteType
import pe.edu.upc.viora.features.plotmanagement.presentation.state.FieldNoteDetailUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.NotePhotoUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.FieldNoteDetailViewModel

/** P79 · Detalle de nota (hoja): the note in full, read-only. It opens once the note is read from the phone. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldNoteDetailSheet(
    noteId: String,
    onDismiss: () -> Unit,
    viewModel: FieldNoteDetailViewModel = hiltViewModel(key = noteId),
) {
    LaunchedEffect(noteId) { viewModel.open(noteId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val note = state.note ?: return
    VioraBottomSheet(onDismissRequest = onDismiss) {
        FieldNoteDetailContent(note = note, state = state, onRetryPhoto = viewModel::retryPhoto, onClose = onDismiss)
    }
}

@Composable
private fun FieldNoteDetailContent(
    note: FieldNote,
    state: FieldNoteDetailUiState,
    onRetryPhoto: () -> Unit,
    onClose: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val day = DateTimeFormatter.ofPattern(stringResource(R.string.note_date_pattern), locale).format(note.observedOn)
    val plot = state.plotName ?: stringResource(R.string.logbook_unknown_plot)
    // "Nota de\nriego": the second line goes in italics, as every title of the app.
    val title = stringResource(R.string.note_detail_title, stringResource(note.type.titleRes())).split('\n')
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
                Column {
                    Text(title.first(), style = style, color = Neutral900)
                    Text(title.last(), style = style, fontStyle = FontStyle.Italic, color = Neutral900)
                }
                Text(
                    stringResource(R.string.logbook_note_details, plot, day),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = Neutral600,
                )
            }
            CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onClose)
        }

        SyncChip(note.syncState, Modifier.padding(top = 16.dp))
        if (note.syncState == FieldNoteSyncState.FAILED) FailedNotice(Modifier.padding(top = 12.dp))

        Column(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Neutral0)
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(R.string.note_text_label), style = MaterialTheme.typography.bodySmall, color = Neutral600)
            Text(note.text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp), color = Neutral900)
        }

        NotePhoto(state.photo, onRetryPhoto, Modifier.padding(top = 12.dp))
    }
}

/** Where the note is: the same chips as the round summary (P54). */
@Composable
private fun SyncChip(syncState: FieldNoteSyncState, modifier: Modifier = Modifier) {
    when (syncState) {
        FieldNoteSyncState.SYNCED -> Chip(R.drawable.ic_task_alt, R.string.note_detail_synced, Green200, Green800, modifier)
        FieldNoteSyncState.PENDING -> Chip(R.drawable.ic_cloud_sync, R.string.note_detail_on_phone, Harvest100, Harvest800, modifier)
        FieldNoteSyncState.FAILED -> Chip(R.drawable.ic_warning, R.string.note_detail_failed, Terracotta100, Terracotta700, modifier)
    }
}

@Composable
private fun Chip(@DrawableRes icon: Int, @StringRes text: Int, background: Color, content: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        }
        Text(
            stringResource(text),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
            color = content,
        )
    }
}

@Composable
private fun FailedNotice(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Terracotta100)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(stringResource(R.string.logbook_note_failed), style = MaterialTheme.typography.titleSmall, color = Terracotta700)
        Text(stringResource(R.string.note_detail_failed_body), style = MaterialTheme.typography.bodySmall, color = Terracotta700)
    }
}

@Composable
private fun NotePhoto(photo: NotePhotoUiState, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    when (photo) {
        NotePhotoUiState.None -> Unit
        NotePhotoUiState.Loading -> Box(
            modifier = modifier
                .fillMaxWidth()
                .height(PhotoMinHeight)
                .clip(PhotoShape)
                .background(Green200),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = Green900)
        }
        // There is no photo viewer, so the sheet shows as much of the picture as it reasonably can.
        is NotePhotoUiState.Ready -> AsyncImage(
            model = File(photo.path),
            contentDescription = stringResource(R.string.note_detail_photo_description),
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = PhotoMinHeight, max = PhotoMaxHeight)
                .clip(PhotoShape)
                .background(Green200),
        )
        NotePhotoUiState.Unavailable -> Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(PhotoShape)
                .background(Neutral0)
                .clickable(role = Role.Button, onClick = onRetry)
                .padding(start = 14.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Neutral100), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_photo_camera), contentDescription = null, tint = Neutral700, modifier = Modifier.size(22.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.note_detail_photo_offline), style = MaterialTheme.typography.titleSmall, color = Neutral900)
                Text(stringResource(R.string.note_detail_photo_retry), style = MaterialTheme.typography.bodySmall, color = Neutral600)
            }
        }
    }
}

private val PhotoShape = RoundedCornerShape(24.dp)
private val PhotoMinHeight = 188.dp
private val PhotoMaxHeight = 360.dp

private fun FieldNoteType.titleRes(): Int = when (this) {
    FieldNoteType.IRRIGATION -> R.string.note_type_irrigation_title
    FieldNoteType.PRUNING -> R.string.note_type_pruning_title
    FieldNoteType.FERTILIZATION -> R.string.note_type_fertilization_title
    FieldNoteType.PESTS -> R.string.note_type_pests_title
    FieldNoteType.OTHER -> R.string.note_type_other_title
}
