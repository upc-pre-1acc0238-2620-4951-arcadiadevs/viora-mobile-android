package pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.AssignPlotSectorUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.GetLicenseSectorsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.GetPlotsWithoutSectorUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.MarkSectorPromptAnsweredUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorChoice
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotSectorsUiState
import pe.edu.upc.viora.features.subscription.application.usecase.GetLatestDataSharingConsentUseCase

/** P06b · Sector de tus lotes: asks once where the plots the member had before joining are. */
@HiltViewModel
class PlotSectorsViewModel @Inject constructor(
    private val plotsWithoutSector: GetPlotsWithoutSectorUseCase,
    private val licenseSectors: GetLicenseSectorsUseCase,
    private val latestConsent: GetLatestDataSharingConsentUseCase,
    private val assignSector: AssignPlotSectorUseCase,
    private val markAnswered: MarkSectorPromptAnsweredUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlotSectorsUiState())
    val uiState: StateFlow<PlotSectorsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val plots = plotsWithoutSector(onlyIfUnanswered = false)
            val sectors = (licenseSectors() as? AppResult.Success)?.value.orEmpty()
            _uiState.update { it.copy(loading = false, plots = plots, sectors = sectors) }
            val manager = (latestConsent() as? AppResult.Success)?.value?.managerName
            _uiState.update { it.copy(managerName = manager) }
        }
    }

    fun choose(plotId: String, choice: SectorChoice) {
        _uiState.update { it.copy(choices = it.choices + (plotId to choice), saveFailed = false) }
    }

    /** «Guardar»: sends every answer. The plots whose sector got through leave the list; the rest stay to retry. */
    fun save() {
        val current = _uiState.value
        if (!current.canSave) return
        _uiState.update { it.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            val saved = current.choices.filter { (plotId, choice) -> assignSector(plotId, choice) is AppResult.Success }.keys
            val failed = saved.size < current.choices.size
            if (!failed) markAnswered()
            _uiState.update {
                it.copy(
                    saving = false,
                    plots = it.plots.filterNot { plot -> plot.id.value in saved },
                    choices = it.choices - saved,
                    saveFailed = failed,
                    done = !failed,
                )
            }
        }
    }

    /** «Más tarde»: not asked again; nothing is sent. */
    fun later() {
        viewModelScope.launch {
            markAnswered()
            _uiState.update { it.copy(done = true) }
        }
    }
}

/** Decides on the Home whether to open P06b: only for a member with plots without a sector, and only once. */
@HiltViewModel
class SectorPromptViewModel @Inject constructor(
    private val plotsWithoutSector: GetPlotsWithoutSectorUseCase,
) : ViewModel() {

    private val _ask = MutableStateFlow(false)
    val ask: StateFlow<Boolean> = _ask.asStateFlow()

    init {
        viewModelScope.launch { _ask.value = plotsWithoutSector(onlyIfUnanswered = true).isNotEmpty() }
    }

    /** P06b was opened; this Home does not open it again. */
    fun opened() {
        _ask.value = false
    }
}
