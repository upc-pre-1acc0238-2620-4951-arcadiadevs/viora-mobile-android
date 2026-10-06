package pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.SettleHarvestUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CalibreReading
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CommercialSizeGrade
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleDialog
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleHarvestRules
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleHarvestUiState
import pe.edu.upc.viora.features.phenology.presentation.ui.tonnesPerHectare
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/**
 * Logic of the settle harvest form (P71) and its confirm / already-settled dialogs (P72): the
 * kilos, date, ticket and calibre the producer is typing, their validation, the live total and the
 * settle call. The plot and campaign come from the route arguments (`plotId`, `campaignYear`).
 *
 * One `Idempotency-Key` is generated per screen and reused on every attempt, so a retry after an
 * ambiguous failure never creates a second settlement.
 */
@HiltViewModel
class SettleHarvestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlot: ObservePlotUseCase,
    private val settleHarvest: SettleHarvestUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "SettleHarvestRoute needs a plotId" }
    private val campaignYear: Int = checkNotNull(savedStateHandle.get<Int>("campaignYear")) { "SettleHarvestRoute needs a campaignYear" }
    private val idempotencyKey: String = UUID.randomUUID().toString()

    private data class Draft(
        val weighedOn: LocalDate,
        val greenText: String = "",
        val blackText: String = "",
        val millTicket: String = "",
        val calibreText: String = "",
        val calibreGrade: CommercialSizeGrade? = null,
        val isSaving: Boolean = false,
        val error: AppError? = null,
        val dialog: SettleDialog? = null,
    )

    private val draft = MutableStateFlow(Draft(weighedOn = today()))

    val uiState: StateFlow<SettleHarvestUiState> = combine(draft, observePlot(PlotId(plotId))) { draft, plot -> build(draft, plot) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), build(draft.value, null))

    fun onWeighedOnChange(date: LocalDate) {
        if (SettleHarvestRules.isFuture(date, today())) return
        edit { it.copy(weighedOn = date) }
    }

    fun onGreenChange(text: String) = edit { it.copy(greenText = text.filter(::isKilosChar)) }

    fun onBlackChange(text: String) = edit { it.copy(blackText = text.filter(::isKilosChar)) }

    fun onMillTicketChange(text: String) = edit { it.copy(millTicket = text.take(SettleHarvestRules.MILL_TICKET_MAX)) }

    /** Typing a count; only digits and a decimal separator are kept. */
    fun onCalibreTextChange(text: String) = edit { it.copy(calibreText = text.filter { c -> c.isDigit() || c == '.' || c == ',' }, calibreGrade = null) }

    /** Picks a grade from the dropdown, or clears it with null (the typed count is empty again). */
    fun onCalibreGradeSelect(grade: CommercialSizeGrade?) = edit { it.copy(calibreGrade = grade, calibreText = "") }

    /** "Asentar cosecha": opens the confirm dialog when the form is valid. */
    fun requestConfirm() {
        if (!uiState.value.canSave) return
        draft.update { it.copy(dialog = SettleDialog.Confirm, error = null) }
    }

    /** "Revisar los kilos" / "Entendido": closes the dialog; ignored while the settle call runs. */
    fun dismissDialog() {
        if (draft.value.isSaving) return
        draft.update { it.copy(dialog = null) }
    }

    /**
     * Settles the harvest. [onSettled] runs when the server created it, [onQueued] when the phone
     * was offline and saved it to sync later; an already-settled campaign opens the 409 dialog and a
     * failure closes the dialog and keeps the form with the error.
     */
    fun confirm(onSettled: (plotId: String, year: Int) -> Unit, onQueued: (plotId: String, year: Int) -> Unit) {
        val current = draft.value
        if (current.dialog != SettleDialog.Confirm || current.isSaving) return
        val settleDraft = toSettleDraft(current) ?: return
        draft.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val result = settleHarvest(settleDraft, idempotencyKey)) {
                is AppResult.Success -> when (val outcome = result.value) {
                    is SettleOutcome.Settled -> finish(onSettled)
                    is SettleOutcome.Queued -> finish(onQueued)
                    is SettleOutcome.AlreadySettled -> draft.update { it.copy(isSaving = false, dialog = SettleDialog.AlreadySettled(outcome.existing)) }
                }
                is AppResult.Failure -> draft.update { it.copy(isSaving = false, dialog = null, error = result.error) }
            }
        }
    }

    private fun finish(callback: (String, Int) -> Unit) {
        draft.update { it.copy(isSaving = false, dialog = null) }
        callback(plotId, campaignYear)
    }

    private fun edit(change: (Draft) -> Draft) {
        draft.update { if (it.isSaving) it else change(it).copy(error = null) }
    }

    private fun toSettleDraft(draft: Draft): SettleHarvestDraft? {
        val green = SettleHarvestRules.kilos(draft.greenText) ?: return null
        val black = SettleHarvestRules.kilos(draft.blackText) ?: return null
        val calibre = SettleHarvestRules.calibre(draft.calibreGrade, draft.calibreText)
        if (calibre == CalibreReading.Invalid) return null
        return SettleHarvestDraft(
            plotId = plotId,
            campaignYear = campaignYear,
            greenOlivesKg = green,
            blackOlivesKg = black,
            weighedOn = draft.weighedOn,
            millTicketNumber = SettleHarvestRules.millTicket(draft.millTicket),
            commercialFruitsPerKg = (calibre as? CalibreReading.Value)?.fruitsPerKg,
        )
    }

    private fun build(draft: Draft, plot: Plot?): SettleHarvestUiState {
        val today = today()
        val total = SettleHarvestRules.totalKilos(draft.greenText, draft.blackText)
        val validTotal = SettleHarvestRules.isValidTotal(total)
        val green = SettleHarvestRules.kilos(draft.greenText)
        val black = SettleHarvestRules.kilos(draft.blackText)
        val calibreError = SettleHarvestRules.calibre(draft.calibreGrade, draft.calibreText) == CalibreReading.Invalid
        val greenError = SettleHarvestRules.kilosError(draft.greenText)
        val blackError = SettleHarvestRules.kilosError(draft.blackText)
        return SettleHarvestUiState(
            plotId = plotId,
            plotName = plot?.name.orEmpty(),
            campaignYear = campaignYear,
            weighedOn = draft.weighedOn,
            maxDate = today,
            greenText = draft.greenText,
            blackText = draft.blackText,
            greenError = greenError,
            blackError = blackError,
            totalKg = total.takeIf { validTotal },
            greenShare = if (validTotal && green != null && black != null) SettleHarvestRules.greenShare(green, black) else null,
            tonnesPerHectare = if (validTotal && total != null && plot != null && plot.areaHectares > 0.0) tonnesPerHectare(total, plot.areaHectares) else null,
            millTicket = draft.millTicket,
            calibreText = draft.calibreText,
            calibreGrade = draft.calibreGrade,
            calibreError = calibreError,
            canSave = validTotal && !calibreError && !draft.isSaving &&
                !SettleHarvestRules.isFuture(draft.weighedOn, today) && SettleHarvestRules.isMillTicketValid(draft.millTicket),
            isSaving = draft.isSaving,
            error = draft.error,
            dialog = draft.dialog,
        )
    }

    private fun today(): LocalDate = LocalDate.now(clock)

    private fun isKilosChar(c: Char): Boolean = c.isDigit() || c == '.' || c == ',' || c == '-' || c.isWhitespace()

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
