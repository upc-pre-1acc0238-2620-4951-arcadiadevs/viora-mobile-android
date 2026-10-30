package pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ConfirmThinningExecutionUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningPrescriptionUseCase
import pe.edu.upc.viora.features.croploadregulation.presentation.state.RegisterThinningUiState
import pe.edu.upc.viora.core.presentation.requirePlotId

/** P62 · Registrar aclareo, opened from the prescription card of P61 (so the plan is already cached). */
@HiltViewModel
class RegisterThinningViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observePrescription: ObserveThinningPrescriptionUseCase,
    private val confirmExecution: ConfirmThinningExecutionUseCase,
    clock: Clock,
) : ViewModel() {

    val plotId: String = savedStateHandle.requirePlotId().value
    val campaignYear: Int = savedStateHandle.get<Int>("campaignYear")?.takeIf { it > 0 } ?: LocalDate.now(clock).year

    private val today = LocalDate.now(clock)

    private val _uiState = MutableStateFlow(
        RegisterThinningUiState(
            plotName = savedStateHandle.get<String>("plotName").orEmpty(),
            campaignYear = campaignYear,
            executedOn = today,
            earliest = today,
            today = today,
        ),
    )
    val uiState: StateFlow<RegisterThinningUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val plan = observePrescription(plotId, campaignYear).first()?.prescription?.takeIf { it.isIssued }
            val prescribed = plan?.percentageToRemove?.roundToInt()?.coerceIn(0, 100)
            // Before the full bloom nothing can be thinned; before the window opens the backend refuses the date.
            val earliest = plan?.windowOpensOn ?: plan?.fullBloomOn ?: LocalDate.of(campaignYear, 1, 1)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    prescriptionId = plan?.id,
                    prescribedPercentage = prescribed,
                    windowOpensOn = plan?.windowOpensOn,
                    windowClosesOn = plan?.windowClosesOn,
                    earliest = earliest,
                    percentage = prescribed ?: 0,
                )
            }
        }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { state ->
            if (state.isSaving || date.isBefore(state.earliest) || date.isAfter(state.today)) state
            else state.copy(executedOn = date, error = null)
        }
    }

    /** Steps the share by ±[RegisterThinningUiState.PERCENT_STEP] within 0–100 %. */
    fun stepPercentage(direction: Int) {
        _uiState.update { state ->
            if (state.isSaving) return@update state
            val next = state.percentage + direction * RegisterThinningUiState.PERCENT_STEP
            state.copy(percentage = next.coerceIn(0, 100), error = null)
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { if (it.isSaving) it else it.copy(note = note.take(RegisterThinningUiState.MAX_NOTE_LENGTH), error = null) }
    }

    /** Sends the thinning; [onSaved] opens P63 once the backend answered. */
    fun save(onSaved: (prescriptionId: String) -> Unit) {
        val state = _uiState.value
        val prescriptionId = state.prescriptionId ?: return
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val result = confirmExecution(
                plotId = plotId,
                campaignYear = campaignYear,
                prescriptionId = prescriptionId,
                executedOn = state.executedOn,
                removalPercentage = state.percentage.toDouble(),
                notes = state.note,
            )
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onSaved(prescriptionId)
                }
                is AppResult.Failure -> _uiState.update { it.copy(isSaving = false, error = result.error) }
            }
        }
    }
}
