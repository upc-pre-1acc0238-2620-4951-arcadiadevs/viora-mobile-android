package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.GetFieldNotePhotoUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveFieldNotesUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.presentation.state.FieldNoteDetailUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.NotePhotoUiState

/**
 * P79 · Detalle de nota. The note comes from the phone, so it reads without signal. Its photo stays on the phone only
 * until it is uploaded; after that it is downloaded the first time the note is opened.
 */
@HiltViewModel
class FieldNoteDetailViewModel @Inject constructor(
    observeNotes: ObserveFieldNotesUseCase,
    observePlots: ObservePlotsUseCase,
    private val getPhoto: GetFieldNotePhotoUseCase,
) : ViewModel() {

    private val noteId = MutableStateFlow<String?>(null)
    private val downloadedPhoto = MutableStateFlow<NotePhotoUiState>(NotePhotoUiState.Loading)

    private val note: Flow<FieldNote?> = combine(noteId, observeNotes()) { id, notes -> notes.firstOrNull { it.id == id } }

    val uiState: StateFlow<FieldNoteDetailUiState> = combine(note, observePlots(), downloadedPhoto) { note, plots, downloaded ->
        FieldNoteDetailUiState(
            note = note,
            plotName = plots.firstOrNull { it.id.value == note?.plotId }?.name,
            photo = when {
                note == null || !note.hasPhoto -> NotePhotoUiState.None
                note.localPhotoPath != null -> NotePhotoUiState.Ready(note.localPhotoPath)
                else -> downloaded
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FieldNoteDetailUiState())

    init {
        // A note just synced loses its copy on the phone while it is open: that also asks for the download.
        viewModelScope.launch {
            note.filterNotNull()
                .map { it.takeIf { note -> note.hasPhoto && note.localPhotoPath == null } }
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .filterNotNull()
                .collect { download(it) }
        }
    }

    fun open(id: String) {
        noteId.value = id
    }

    fun retryPhoto() {
        val id = noteId.value ?: return
        val current = uiState.value.note?.takeIf { it.id == id } ?: return
        viewModelScope.launch { download(current) }
    }

    private suspend fun download(note: FieldNote) {
        downloadedPhoto.value = NotePhotoUiState.Loading
        downloadedPhoto.value = when (val result = getPhoto(note.plotId, note.id)) {
            is AppResult.Success -> NotePhotoUiState.Ready(result.value)
            // The server has no such photo: nothing will change by trying again.
            is AppResult.Failure -> if (result.error is AppError.NotFound) NotePhotoUiState.None else NotePhotoUiState.Unavailable
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
