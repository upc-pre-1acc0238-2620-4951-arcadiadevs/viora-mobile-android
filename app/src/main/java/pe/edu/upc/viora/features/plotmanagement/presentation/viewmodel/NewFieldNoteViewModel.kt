package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.home.application.usecase.ObserveChosenPlotUseCase
import pe.edu.upc.viora.features.home.domain.FocusedPlotRule
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.WriteFieldNoteUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteType
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewFieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.service.NotePhotoStore
import pe.edu.upc.viora.features.plotmanagement.presentation.state.NewFieldNoteUiState

/**
 * P75 · Nueva nota. The plot preselected is the one the Home talks about; the note is saved on the
 * phone and sent in the background, so saving works without signal. A photo is reduced right away.
 */
@HiltViewModel
class NewFieldNoteViewModel @Inject constructor(
    observePlots: ObservePlotsUseCase,
    observeChosenPlot: ObserveChosenPlotUseCase,
    private val writeNote: WriteFieldNoteUseCase,
    private val photoStore: NotePhotoStore,
    clock: Clock,
) : ViewModel() {

    private val mutableState = MutableStateFlow(NewFieldNoteUiState(today = LocalDate.now(clock)))
    val uiState: StateFlow<NewFieldNoteUiState> = mutableState.asStateFlow()

    private val savedEvents = Channel<FieldNote>(Channel.BUFFERED)

    /** Emits once the note is saved on the phone. */
    val saved = savedEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(observePlots(), observeChosenPlot()) { plots, chosen -> plots to FocusedPlotRule.pick(plots, chosen) }
                .collect { (plots, focused) ->
                    mutableState.update { state ->
                        val kept = state.plotId?.takeIf { id -> plots.any { it.id.value == id } }
                        state.copy(plots = plots, plotId = kept ?: focused?.id?.value)
                    }
                }
        }
    }

    fun selectPlot(plotId: String) = mutableState.update { it.copy(plotId = plotId) }

    fun selectType(type: FieldNoteType) = mutableState.update { it.copy(type = type) }

    fun changeText(text: String) = mutableState.update { it.copy(text = text.take(FieldNote.MAX_TEXT_LENGTH)) }

    fun attachPhoto(uri: String) {
        mutableState.update { it.copy(isPreparingPhoto = true, photoFailed = false) }
        viewModelScope.launch {
            val path = photoStore.keep(uri)
            val previous = mutableState.value.photoPath
            if (path != null && previous != null) photoStore.discard(previous)
            mutableState.update {
                it.copy(isPreparingPhoto = false, photoPath = path ?: it.photoPath, photoFailed = path == null)
            }
        }
    }

    fun removePhoto() {
        val path = mutableState.value.photoPath ?: return
        mutableState.update { it.copy(photoPath = null) }
        viewModelScope.launch { photoStore.discard(path) }
    }

    fun save() {
        val state = mutableState.value
        val plotId = state.plotId
        if (!state.canSave || plotId == null) return
        mutableState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = writeNote(NewFieldNote(plotId, state.type, state.text, state.today, state.photoPath))
            mutableState.update { it.copy(isSaving = false) }
            if (result is AppResult.Success) {
                // The photo now belongs to the note: closing the sheet must not delete it.
                mutableState.update { NewFieldNoteUiState(plots = it.plots, plotId = it.plotId, today = it.today) }
                savedEvents.send(result.value)
            }
        }
    }

    /** The sheet was closed without saving: the draft is dropped with its reduced photo. */
    fun discardDraft() {
        val path = mutableState.value.photoPath
        mutableState.update { NewFieldNoteUiState(plots = it.plots, plotId = it.plotId, today = it.today) }
        if (path != null) viewModelScope.launch { photoStore.discard(path) }
    }
}
