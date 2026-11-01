package pe.edu.upc.viora.features.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.assistant.application.usecase.LoadDailySummaryUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.ScheduleDailySummaryReminderUseCase
import pe.edu.upc.viora.features.assistant.presentation.state.DailySummaryCardUiState
import javax.inject.Inject

/**
 * The Viora card of Inicio: the summary of the day of the whole field. Opening Inicio also keeps the morning
 * reminder scheduled, since signing out cancels it.
 */
@HiltViewModel
class DailySummaryViewModel @Inject constructor(
    private val loadDailySummary: LoadDailySummaryUseCase,
    private val scheduleReminder: ScheduleDailySummaryReminderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailySummaryCardUiState>(DailySummaryCardUiState.Loading)
    val uiState: StateFlow<DailySummaryCardUiState> = _uiState.asStateFlow()

    init {
        scheduleReminder()
    }

    /**
     * Reads the summary of the day; Inicio calls it each time it comes back to the front, so a phone left open
     * overnight shows the new day's. Serving a summary already written spends none of the producer's answers.
     * A summary on screen stays while it loads.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.value = when (val result = loadDailySummary()) {
                is AppResult.Success -> DailySummaryCardUiState.Ready(result.value)
                is AppResult.Failure -> when (result.error) {
                    // 403 no plan, 409 no active plots: the assistant has nothing to offer here
                    is AppError.Forbidden, is AppError.Conflict -> DailySummaryCardUiState.Hidden
                    else -> (_uiState.value as? DailySummaryCardUiState.Ready) ?: DailySummaryCardUiState.WithoutSummary
                }
            }
        }
    }
}
