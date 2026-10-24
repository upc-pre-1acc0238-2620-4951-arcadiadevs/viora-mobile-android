package pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningExecutionUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObserveThinningPrescriptionUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RecordFullBloomUseCase
import pe.edu.upc.viora.features.croploadregulation.application.usecase.RefreshThinningPrescriptionUseCase
import pe.edu.upc.viora.features.croploadregulation.presentation.state.FullBloomSheetState
import pe.edu.upc.viora.features.croploadregulation.presentation.state.ThinningPlanUiState
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

/** P61 · Plan del lote and its P64 full bloom sheet. */
@HiltViewModel
class ThinningPlanViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePrescription: ObserveThinningPrescriptionUseCase,
    observeExecution: ObserveThinningExecutionUseCase,
    observePlot: ObservePlotUseCase,
    private val refreshPrescription: RefreshThinningPrescriptionUseCase,
    private val recordFullBloom: RecordFullBloomUseCase,
    private val clock: Clock,
) : ViewModel() {

    val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "ThinningPlanRoute requires plotId" }
    private val initialPlotName: String = savedStateHandle.get<String>("plotName").orEmpty()

    /** The campaign of the plan: the one the route names, or the current year. */
    val campaignYear: Int = savedStateHandle.get<Int>("campaignYear")?.takeIf { it > 0 } ?: LocalDate.now(clock).year

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val hasFinishedOnce: Boolean = false,
        val error: AppError? = null,
    )

    private val refreshState = MutableStateFlow(RefreshState())

    private val _sheet = MutableStateFlow<FullBloomSheetState?>(null)

    /** The P64 sheet while it is open. */
    val sheet: StateFlow<FullBloomSheetState?> = _sheet.asStateFlow()

    val uiState: StateFlow<ThinningPlanUiState> = combine(
        observePrescription(plotId, campaignYear),
        observePlot(PlotId(plotId)),
        observeExecution(plotId, campaignYear),
        refreshState,
    ) { cached, plot, execution, refresh ->
        when {
            cached != null -> ThinningPlanUiState.Content(
                plotName = plot?.name ?: initialPlotName,
                variety = plot?.variety,
                areaHectares = plot?.areaHectares,
                campaignYear = campaignYear,
                prescription = cached.prescription,
                execution = execution,
                today = LocalDate.now(clock),
                syncedAt = cached.syncedAt,
                isRefreshing = refresh.isRefreshing,
                offline = refresh.error is AppError.Offline || refresh.error is AppError.Timeout,
            )
            refresh.isRefreshing || !refresh.hasFinishedOnce -> ThinningPlanUiState.Loading
            else -> ThinningPlanUiState.Error(refresh.error ?: AppError.Unknown())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThinningPlanUiState.Loading,
    )

    init {
        refresh()
        // From the Home bloom card: open P64 once the plan is on screen, only the first time (not after a rotation).
        if (savedStateHandle.get<Boolean>(RECORD_BLOOM_KEY) == true) {
            viewModelScope.launch {
                uiState.first { it is ThinningPlanUiState.Content }
                savedStateHandle[RECORD_BLOOM_KEY] = false
                openFullBloomSheet()
            }
        }
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            val result = refreshPrescription(plotId, campaignYear)
            refreshState.update {
                it.copy(isRefreshing = false, hasFinishedOnce = true, error = (result as? AppResult.Failure)?.error)
            }
        }
    }

    /** Opens P64 on the recorded bloom, or on today when none was recorded. */
    fun openFullBloomSheet() {
        val today = LocalDate.now(clock)
        val recorded = (uiState.value as? ThinningPlanUiState.Content)?.prescription?.fullBloomOn
        val earliest = LocalDate.of(campaignYear, 1, 1)
        val latest = minOf(today, LocalDate.of(campaignYear, 12, 31))
        _sheet.value = FullBloomSheetState(
            date = (recorded ?: latest).coerceIn(earliest, latest),
            today = latest,
            earliest = earliest,
        )
    }

    fun stepFullBloom(days: Long) {
        _sheet.update { sheet ->
            sheet?.takeUnless { it.isSaving }?.let {
                it.copy(date = it.date.plusDays(days).coerceIn(it.earliest, it.today), error = null)
            } ?: sheet
        }
    }

    fun dismissFullBloomSheet() {
        if (_sheet.value?.isSaving == true) return
        _sheet.value = null
    }

    fun saveFullBloom() {
        val sheet = _sheet.value ?: return
        if (sheet.isSaving) return
        _sheet.value = sheet.copy(isSaving = true, error = null)
        viewModelScope.launch {
            when (val result = recordFullBloom(plotId, campaignYear, sheet.date)) {
                is AppResult.Success -> {
                    _sheet.value = null
                    refreshState.update { it.copy(error = null) }
                }
                is AppResult.Failure -> _sheet.update { it?.copy(isSaving = false, error = result.error) }
            }
        }
    }

    private companion object {
        const val RECORD_BLOOM_KEY = "recordBloom"
    }
}

private fun LocalDate.coerceIn(min: LocalDate, max: LocalDate): LocalDate = when {
    isBefore(min) -> min
    isAfter(max) -> max
    else -> this
}
