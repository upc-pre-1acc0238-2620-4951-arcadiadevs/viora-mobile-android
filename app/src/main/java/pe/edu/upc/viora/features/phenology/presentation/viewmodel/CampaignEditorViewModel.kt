package pe.edu.upc.viora.features.phenology.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveBearingIndexUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RecordHarvestYieldUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RectifyHarvestYieldUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RemoveHarvestRecordUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignChangeKind
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignEditorUiState
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignMode
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignResult
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignRules
import pe.edu.upc.viora.features.phenology.presentation.state.HarvestPresenter

/**
 * Logic of the campaign sheet: the year and kilos the producer is typing, their validation, the
 * live index preview and the save / delete calls. The sheet is closed while [uiState] is null.
 */
@HiltViewModel
class CampaignEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeRecords: ObserveHarvestHistoryUseCase,
    observeIndex: ObserveBearingIndexUseCase,
    private val recordYield: RecordHarvestYieldUseCase,
    private val rectifyYield: RectifyHarvestYieldUseCase,
    private val removeRecord: RemoveHarvestRecordUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "HarvestHistoryRoute needs a plotId" }

    private data class Draft(
        val mode: CampaignMode,
        val year: Int,
        val kilosText: String,
        val isSaving: Boolean = false,
        val error: AppError? = null,
        val confirmingDelete: Boolean = false,
    )

    private val draft = MutableStateFlow<Draft?>(null)

    val uiState: StateFlow<CampaignEditorUiState?> = combine(
        draft,
        observeRecords(plotId),
        observeIndex(plotId),
    ) { draft, records, index -> draft?.let { build(it, records, index) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** Opens the sheet to register a campaign, starting one year before the oldest registered. */
    fun openAdd() {
        viewModelScope.launch {
            val records = observeRecords(plotId).first()
            val year = HarvestPresenter.suggestedYear(records, currentYear())
            draft.value = Draft(CampaignMode.Add, year, kilosText = "")
        }
    }

    /** Opens the sheet to correct (or delete) a registered campaign. */
    fun openCorrect(record: HarvestRecord) {
        draft.value = Draft(CampaignMode.Correct(record), record.campaignYear, kilosText = plainKilos(record.totalYieldKg))
    }

    fun dismiss() {
        if (draft.value?.isSaving == true) return
        draft.value = null
    }

    fun onYearStep(delta: Int) {
        draft.update { d -> if (d != null && d.mode is CampaignMode.Add) d.copy(year = d.year + delta, error = null) else d }
    }

    fun onKilosChange(text: String) {
        draft.update { it?.copy(kilosText = text.filter { c -> c.isDigit() || c == '.' || c == ',' || c == '-' || c.isWhitespace() }, error = null) }
    }

    /** Saves the campaign (add) or its new yield (correct); [onSaved] runs once the server accepted it. */
    fun save(onSaved: (CampaignResult) -> Unit) {
        val state = uiState.value ?: return
        if (!state.canSave || state.isSaving) return
        val kilos = CampaignRules.parseKilos(state.kilosText) ?: return
        draft.update { it?.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val mode = state.mode) {
                CampaignMode.Add -> finish(recordYield(plotId, state.year, kilos), CampaignChangeKind.ADDED, state.year, onSaved) { it.id }
                is CampaignMode.Correct -> finish(
                    rectifyYield(plotId, mode.record.id, kilos),
                    CampaignChangeKind.CORRECTED,
                    mode.record.campaignYear,
                    onSaved,
                ) { it.id }
            }
        }
    }

    fun requestDelete() {
        draft.update { d -> if (d != null && d.mode is CampaignMode.Correct) d.copy(confirmingDelete = true, error = null) else d }
    }

    fun cancelDelete() {
        draft.update { it?.copy(confirmingDelete = false, error = null) }
    }

    fun confirmDelete(onDeleted: (CampaignResult) -> Unit) {
        val current = draft.value ?: return
        val mode = current.mode as? CampaignMode.Correct ?: return
        if (current.isSaving) return
        draft.update { it?.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            finish(removeRecord(plotId, mode.record.id), CampaignChangeKind.DELETED, mode.record.campaignYear, onDeleted) { null }
        }
    }

    private fun <T> finish(
        result: AppResult<T>,
        kind: CampaignChangeKind,
        year: Int,
        onDone: (CampaignResult) -> Unit,
        idOf: (T) -> String?,
    ) {
        when (result) {
            is AppResult.Success -> {
                draft.value = null
                onDone(CampaignResult(kind, year, idOf(result.value)))
            }
            is AppResult.Failure -> draft.update { it?.copy(isSaving = false, error = result.error) }
        }
    }

    private fun build(draft: Draft, records: List<HarvestRecord>, serverIndex: BearingIndex?): CampaignEditorUiState {
        val currentYear = currentYear()
        val registered = records.map { it.campaignYear }
        val current = if (records.size >= HoblynBbi.MIN_CAMPAIGNS) serverIndex?.value ?: HoblynBbi.index(records) else null
        val kilos = CampaignRules.parseKilos(draft.kilosText)
        val kilosValid = CampaignRules.isValidKilos(kilos)
        val mode = draft.mode
        val yearError = when (mode) {
            CampaignMode.Add -> CampaignRules.yearError(draft.year, registered.toSet(), currentYear)
            is CampaignMode.Correct -> null
        }
        val preview = if (kilosValid && yearError == null && kilos != null) {
            when (mode) {
                CampaignMode.Add -> CampaignRules.previewAdd(records, current, draft.year, kilos)
                is CampaignMode.Correct -> CampaignRules.previewCorrect(records, current, draft.year, kilos)
            }
        } else {
            null
        }
        val changed = when (mode) {
            CampaignMode.Add -> true
            is CampaignMode.Correct -> kilos != mode.record.totalYieldKg
        }
        return CampaignEditorUiState(
            mode = mode,
            year = draft.year,
            kilosText = draft.kilosText,
            yearError = yearError,
            yearCaption = CampaignRules.yearCaption(draft.year, registered),
            kilosError = draft.kilosText.isNotBlank() && !kilosValid,
            preview = preview,
            canSave = kilosValid && yearError == null && changed && !draft.isSaving,
            isSaving = draft.isSaving,
            error = draft.error,
            confirmingDelete = draft.confirmingDelete,
            deletePreview = if (mode is CampaignMode.Correct) CampaignRules.previewDelete(records, current, draft.year) else null,
            maxYear = currentYear,
        )
    }

    private fun currentYear(): Int = LocalDate.now(clock).year

    /** 22000.0 as "22000", 22000.5 as "22000.5": what goes into the field when correcting. */
    private fun plainKilos(kg: Double): String =
        if (kg == kg.toLong().toDouble()) kg.toLong().toString() else kg.toString()

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
