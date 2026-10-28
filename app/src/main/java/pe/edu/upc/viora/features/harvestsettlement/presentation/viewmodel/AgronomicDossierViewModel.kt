package pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.DownloadDossierPdfUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.FindLatestDossierCampaignUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveAgronomicDossierUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.SealAgronomicDossierUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.VerifyDossierUseCase
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.AgronomicDossierUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.DossierVerificationState

/**
 * P76 · Expediente del lote with its P77 and P78 sheets. Opening it seals the dossier of the campaign (the backend
 * answers the stored one when it was already sealed, so this is also the download); without signal the cached dossier
 * and its last PDF are shown. Campaign 0 means the newest settled campaign of the plot ("Más opciones").
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AgronomicDossierViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDossier: ObserveAgronomicDossierUseCase,
    private val observeSettledHarvests: ObserveSettledHarvestsUseCase,
    private val findLatestCampaign: FindLatestDossierCampaignUseCase,
    private val sealDossier: SealAgronomicDossierUseCase,
    private val downloadPdf: DownloadDossierPdfUseCase,
    private val verifyDossier: VerifyDossierUseCase,
) : ViewModel() {

    val plotId: String = checkNotNull(savedStateHandle.get<String>("plotId")) { "AgronomicDossierRoute requires plotId" }
    private val plotName: String = savedStateHandle.get<String>("plotName").orEmpty()
    private val requestedYear: Int? = savedStateHandle.get<Int>("campaignYear")?.takeIf { it > 0 }

    private data class Status(
        val resolving: Boolean = true,
        val noDossier: Boolean = false,
        val error: AppError? = null,
        val offline: Boolean = false,
        val downloading: Boolean = false,
        val downloadFailed: Boolean = false,
    )

    private val campaignYear = MutableStateFlow(requestedYear)
    private val status = MutableStateFlow(Status())

    val uiState: StateFlow<AgronomicDossierUiState> = combine(
        campaignYear.flatMapLatest { year -> if (year == null) flowOf(null) else observeDossier(plotId, year) },
        status,
    ) { cached, status ->
        when {
            cached != null -> AgronomicDossierUiState.Content(
                dossier = cached.dossier,
                lastPdf = cached.pdfPath?.let(::File)?.takeIf { it.exists() },
                lastPdfSavedAt = cached.pdfSavedAt,
                offline = status.offline,
                downloading = status.downloading,
                downloadFailed = status.downloadFailed,
            )
            status.resolving -> AgronomicDossierUiState.Loading
            status.noDossier -> AgronomicDossierUiState.NoDossier(plotName)
            else -> AgronomicDossierUiState.Error(status.error ?: AppError.NotFound())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AgronomicDossierUiState.Loading,
    )

    private val _pdfSheet = MutableStateFlow<File?>(null)

    /** P77 · PDF listo, open while not null. */
    val pdfSheet: StateFlow<File?> = _pdfSheet.asStateFlow()

    private val _verification = MutableStateFlow<DossierVerificationState?>(null)

    /** P78 · Verificar autenticidad, open while not null. */
    val verification: StateFlow<DossierVerificationState?> = _verification.asStateFlow()

    init {
        load()
    }

    fun load() {
        status.update { it.copy(resolving = true, error = null, noDossier = false) }
        viewModelScope.launch {
            val year = campaignYear.value ?: resolveCampaign()
            if (year == null) return@launch
            campaignYear.value = year
            val result = sealDossier(plotId, year)
            val error = (result as? AppResult.Failure)?.error
            status.update {
                it.copy(resolving = false, error = error, offline = error.isConnectivity)
            }
        }
    }

    /** "Descargar PDF" and "Compartir": the PDF already on the phone opens P77 at once, otherwise it is downloaded. */
    fun openPdf() {
        val content = uiState.value as? AgronomicDossierUiState.Content ?: return
        content.lastPdf?.let { _pdfSheet.value = it; return }
        if (status.value.downloading) return
        status.update { it.copy(downloading = true, downloadFailed = false) }
        viewModelScope.launch {
            when (val result = downloadPdf(plotId, content.dossier.campaignYear)) {
                is AppResult.Success -> {
                    status.update { it.copy(downloading = false, offline = false) }
                    _pdfSheet.value = result.value
                }
                is AppResult.Failure -> status.update {
                    it.copy(
                        downloading = false,
                        downloadFailed = true,
                        offline = it.offline || result.error.isConnectivity,
                    )
                }
            }
        }
    }

    fun closePdf() {
        _pdfSheet.value = null
    }

    /** The code card: asks the Viora record whether the dossier's code is one it stores. */
    fun verify() {
        val content = uiState.value as? AgronomicDossierUiState.Content ?: return
        _verification.value = DossierVerificationState.Checking
        viewModelScope.launch {
            _verification.value = when (val result = verifyDossier(content.dossier.verificationHash)) {
                is AppResult.Success -> DossierVerificationState.Matches(result.value)
                is AppResult.Failure ->
                    if (result.error is AppError.NotFound) DossierVerificationState.NoMatch else DossierVerificationState.Unreachable
            }
        }
    }

    fun closeVerification() {
        _verification.value = null
    }

    /**
     * The newest campaign with a dossier or a settlement of the plot. Null when there is none (no dossier to show) or
     * when nothing could be asked and nothing is cached (the error is kept).
     */
    private suspend fun resolveCampaign(): Int? {
        val sealed = findLatestCampaign(plotId)
        val settled = observeSettledHarvests().first().filter { it.plotId == plotId }.maxOfOrNull { it.campaignYear }
        val sealedYear = (sealed as? AppResult.Success)?.value
        val year = listOfNotNull(sealedYear, settled).maxOrNull()
        if (year == null) {
            status.update {
                when (sealed) {
                    is AppResult.Success -> it.copy(resolving = false, noDossier = true)
                    is AppResult.Failure -> it.copy(resolving = false, error = sealed.error)
                }
            }
        }
        return year
    }
}
